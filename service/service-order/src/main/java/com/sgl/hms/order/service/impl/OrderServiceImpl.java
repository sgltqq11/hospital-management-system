package com.sgl.hms.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sgl.hms.common.exception.CustomHmsGlobalException;
import com.sgl.hms.common.helper.HttpRequestHelper;
import com.sgl.hms.common.result.ResultCodeEnum;
import com.sgl.hms.common.service.RabbitService;
import com.sgl.hms.common.util.RabbitMQConst;
import com.sgl.hms.enums.OrderStatusEnum;
import com.sgl.hms.hosp.client.HospitalFeignClient;
import com.sgl.hms.model.order.OrderInfo;
import com.sgl.hms.model.order.PaymentInfo;
import com.sgl.hms.model.user.Patient;
import com.sgl.hms.order.mapper.OrderInfoMapper;
import com.sgl.hms.order.mapper.PaymentInfoMapper;
import com.sgl.hms.order.service.OrderService;
import com.sgl.hms.order.service.WeiXinService;
import com.sgl.hms.user.client.PatientFeignClient;
import com.sgl.hms.vo.hosp.ScheduleOrderVo;
import com.sgl.hms.vo.order.*;
import com.sgl.hms.vo.sms.SmsVo;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {


    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    @Autowired
    private PatientFeignClient patientFeignClient;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private WeiXinService weiXinService;

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;


    //创建订单
    @Override
    public Long saveOrder(String scheduleId, Long patientId) {

        //获取就诊人信息  （获取hms_user数据库的patient表数据，对接service-user模块下PatientFeignController的方法）
        Patient patient = patientFeignClient.getPatientOrder(patientId);

        //获取排班相关信息 (获取mongoDB数据库hms_hosp中schedule表中的数据并封装其他值)
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);

        //判断当前时间是否还可以预约
        if(new DateTime(scheduleOrderVo.getStartTime()).isAfterNow()
                || new DateTime(scheduleOrderVo.getEndTime()).isBeforeNow()) {
            throw new CustomHmsGlobalException(ResultCodeEnum.TIME_NO);
        }

        //获取签名信息  (获取数据库hms_hosp中hospital_set表中的数据封装并返回)
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(scheduleOrderVo.getHoscode());

        //添加到订单表
        OrderInfo orderInfo = new OrderInfo();
        //scheduleOrderVo 数据复制到 orderInfo
        BeanUtils.copyProperties(scheduleOrderVo,orderInfo,OrderInfo.class);
        //再向orderInfo设置其他数据
        String outTradeNo = System.currentTimeMillis() + ""+ new Random().nextInt(100);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setScheduleId(scheduleOrderVo.getHosScheduleId());
        orderInfo.setUserId(patient.getUserId());
        orderInfo.setPatientId(patientId);
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientPhone(patient.getPhone());
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());

        //添加到hms_order数据库中order_info表
        baseMapper.insert(orderInfo);

        /*调用医院接口，实现预约挂号操作 （
        hospital-manager的hospital-manage/src/main/java/com/sgl/hospital/controller/HospitalController.java
        中的AgreeAccountLendProject方法 路径@PostMapping("/order/submitOrder")）
        设置调用医院接口需要参数，参数放到map集合*/
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",orderInfo.getHoscode());
        paramMap.put("depcode",orderInfo.getDepcode());
        paramMap.put("hosScheduleId",orderInfo.getScheduleId());
        paramMap.put("reserveDate",new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", orderInfo.getReserveTime());
        paramMap.put("amount",orderInfo.getAmount());

        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType",patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex",patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone",patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode",patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode",patient.getDistrictCode());
        paramMap.put("address",patient.getAddress());
        //联系人
        paramMap.put("contactsName",patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo",patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone",patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());

        String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
        paramMap.put("sign", sign);

        //请求医院系统接口 (操作hospital-manager模块中order_info和schedule表并返回相关数据)
        JSONObject result = HttpRequestHelper.sendRequest(paramMap, signInfoVo.getApiUrl() + "/order/submitOrder");

        if(result.getInteger("code")==200) {
            JSONObject jsonObject = result.getJSONObject("data");
            //预约记录唯一标识（医院预约记录主键）
            String hosRecordId = jsonObject.getString("hosRecordId");
            //预约序号
            Integer number = jsonObject.getInteger("number");;
            //取号时间
            String fetchTime = jsonObject.getString("fetchTime");;
            //取号地址
            String fetchAddress = jsonObject.getString("fetchAddress");;
            //更新订单
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);

            //根据hospital-manager模块返回的相关数据更新hms_order数据库中order_info表数据
            baseMapper.updateById(orderInfo);
            //排班可预约数
            Integer reservedNumber = jsonObject.getInteger("reservedNumber");
            //排班剩余预约数
            Integer availableNumber = jsonObject.getInteger("availableNumber");

            //发送mq消息，号源更新和短信通知
            //发送mq信息更新号源
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(scheduleId);
            orderMqVo.setReservedNumber(reservedNumber);
            orderMqVo.setAvailableNumber(availableNumber);
            //短信提示
            SmsVo smsVo = new SmsVo();
            smsVo.setPhone(orderInfo.getPatientPhone());
            String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime()==0 ? "上午" : "下午");
            Map<String,Object> param = new HashMap<String,Object>(){{
                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                put("amount", orderInfo.getAmount());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
                put("quitTime", new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
            }};
            smsVo.setParam(param);
            orderMqVo.setSmsVo(smsVo);

            //mq发送信息，service-hosp模块监听并更新mongoDB数据库中schedule表的数据
            rabbitService.sendMessage(RabbitMQConst.EXCHANGE_DIRECT_ORDER, RabbitMQConst.ROUTING_ORDER, orderMqVo);

        } else {
            throw new CustomHmsGlobalException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }
        return orderInfo.getId();
    }


    //根据订单id查询订单详情
    @Override
    public OrderInfo getOrderInfo(String orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        orderInfo = this.packOrderInfo(orderInfo);
        if (null == orderInfo){
            return null;
        }
        return orderInfo;
    }

    //把状态status转换为对应的文字描述
    private OrderInfo packOrderInfo(OrderInfo orderInfo) {
        orderInfo.getParam().put("orderStatusString", OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;
    }


    //订单列表（条件查询带分页）
    @Override
    public IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo) {
        //orderQueryVo获取条件值
        String name = orderQueryVo.getKeyword(); //医院名称
        Long patientId = orderQueryVo.getPatientId(); //就诊人id
        String orderStatus = orderQueryVo.getOrderStatus(); //订单状态
        String reserveDate = orderQueryVo.getReserveDate();//安排时间
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();
        Long userId = orderQueryVo.getUserId();//用户id
        String outTradeNo = orderQueryVo.getOutTradeNo();//订单号
        String patientName = orderQueryVo.getPatientName();//就诊人名称
        //对条件值进行非空判断
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(userId)) {
            wrapper.eq("user_id",userId);
        }

        if(!StringUtils.isEmpty(outTradeNo)) {
            wrapper.eq("out_trade_no",outTradeNo);
        }

        if(!StringUtils.isEmpty(name)) {
            wrapper.like("hosname",name);
        }

        if(!StringUtils.isEmpty(patientName)) {
            wrapper.like("patient_name",patientName);
        }

        if(!StringUtils.isEmpty(patientId)) {
            wrapper.eq("patient_id",patientId);
        }
        if(!StringUtils.isEmpty(orderStatus)) {
            wrapper.eq("order_status",orderStatus);
        }
        if(!StringUtils.isEmpty(reserveDate)) {
            wrapper.ge("reserve_date",reserveDate);
        }
        if(!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time",createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time",createTimeEnd);
        }
        //调用mapper的方法
        IPage<OrderInfo> pages = baseMapper.selectPage(pageParam, wrapper);
        //编号变成对应值封装
        pages.getRecords().stream().forEach(item -> {
            this.packOrderInfo(item);
        });
        return pages;
    }

    //订单详情
    @Override
    public Map<String, Object> show(Long orderId) {
        Map<String, Object> map = new HashMap<>();
        //根据订单id查询并封装
        OrderInfo orderInfo = this.packOrderInfo(this.getById(orderId));
        map.put("orderInfo", orderInfo);
        Patient patient
                =  patientFeignClient.getPatientOrder(orderInfo.getPatientId());
        map.put("patient", patient);
        return map;
    }


    //取消预约
    @Override
    public Boolean cancelOrder(Long orderId) {

        //获取订单信息
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        //判断是否取消
        DateTime quitTime = new DateTime(orderInfo.getQuitTime()); //退号时间
        if (quitTime.isBeforeNow()) throw new CustomHmsGlobalException(ResultCodeEnum.CANCEL_ORDER_NO);


        //调用医院接口实现预约取消
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        if(null == signInfoVo) {
            throw new CustomHmsGlobalException(ResultCodeEnum.PARAM_ERROR);
        }
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode",orderInfo.getHoscode());
        reqMap.put("hosRecordId",orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);

        JSONObject result = HttpRequestHelper
                .sendRequest(reqMap, signInfoVo.getApiUrl()+"/order/updateCancelStatus");


        if(result.getInteger("code") != 200) {

            throw new CustomHmsGlobalException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }else {

            //判断当前订单是否可以取消 （已支付可以进行退款）
            if(orderInfo.getOrderStatus().intValue() == OrderStatusEnum.PAID.getStatus().intValue()) {

                //调用微信退款
                Boolean isRefund = weiXinService.refund(orderId);
                //返回false，退款失败
                if(!isRefund) {
                    throw new CustomHmsGlobalException(ResultCodeEnum.CANCEL_ORDER_FAIL);
                }
                //更改订单状态
                orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
                baseMapper.updateById(orderInfo);

                //更新支付记录状态(自己加的，因为支付记录状态还是显示已支付)****
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setPaymentStatus(-1);  //支付已取消
                paymentInfo.setOrderId(orderInfo.getId());
                paymentInfoMapper.updateById(paymentInfo);


                //发送mq信息更新预约数 我们与下单成功更新预约数使用相同的mq信息，不设置可预约数与剩余预约数，接收端可预约数减1即可
                OrderMqVo orderMqVo = new OrderMqVo();
                orderMqVo.setScheduleId(orderInfo.getScheduleId());
                //短信提示
                SmsVo smsVo = new SmsVo();
                smsVo.setPhone(orderInfo.getPatientPhone());
                //smsVo.setTemplateCode("SMS_194640722");
                String reserveDate = new DateTime(
                        orderInfo.getReserveDate()).toString("yyyy-MM-dd") +
                        (orderInfo.getReserveTime()==0 ? "上午": "下午");

                Map<String,Object> param = new HashMap<String,Object>(){{
                    put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                    put("reserveDate", reserveDate);
                    put("name", orderInfo.getPatientName());
                }};
                smsVo.setParam(param);
                orderMqVo.setSmsVo(smsVo);
                rabbitService.sendMessage(RabbitMQConst.EXCHANGE_DIRECT_ORDER, RabbitMQConst.ROUTING_ORDER, orderMqVo);
            }

            return true;
        }
    }


    /**
     * 就医提醒（就诊通知）
     */
    @Override
    public void patientTips() {

        log.info("就医提醒测试："+"记得按时就医哦");

        //根据安排日期和状态查询
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reserve_date", new DateTime().toString("yyyy-MM-dd"));
        queryWrapper.ne("order_status", OrderStatusEnum.CANCLE.getStatus());

        List<OrderInfo> orderInfoList = baseMapper.selectList(queryWrapper);

        for (OrderInfo orderInfo : orderInfoList) {
            //短信提示
            SmsVo smsVo = new SmsVo();
            smsVo.setPhone(orderInfo.getPatientPhone());
            String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime() == 0 ? "上午" : "下午");
            Map<String, Object> param = new HashMap<String, Object>() {{
                put("title", orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
            }};
            smsVo.setParam(param);

            log.info("就医提醒："+"记得按时就医哦");

            //发送mq，发送短信（短信未实现，阿里云未完善）
            rabbitService.sendMessage(RabbitMQConst.EXCHANGE_DIRECT_SMS, RabbitMQConst.ROUTING_SMS_ITEM, smsVo);

        }
    }


    //获取订单统计数据
    @Override
    public Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo) {

        //调用mapper方法得到数据
        List<OrderCountVo> orderCountList = baseMapper.selectOrderCount(orderCountQueryVo);

        //获取x需要数据，日期数据，list集合
        List<String> dateList = orderCountList.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());

        //获取y需要数据，具体数量 list集合
        List<Integer> countList = orderCountList.stream().map(OrderCountVo::getCount).collect(Collectors.toList());

        Map<String,Object> map = new HashMap<>();
        map.put("dateList", dateList);
        map.put("countList", countList);

        return map;
    }

}
