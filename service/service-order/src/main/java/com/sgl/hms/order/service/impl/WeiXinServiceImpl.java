package com.sgl.hms.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.sgl.hms.enums.PaymentTypeEnum;
import com.sgl.hms.enums.RefundStatusEnum;
import com.sgl.hms.model.order.OrderInfo;
import com.sgl.hms.model.order.PaymentInfo;
import com.sgl.hms.model.order.RefundInfo;
import com.sgl.hms.order.service.OrderService;
import com.sgl.hms.order.service.PaymentService;
import com.sgl.hms.order.service.RefundService;
import com.sgl.hms.order.service.WeiXinService;
import com.sgl.hms.order.util.ConstantPropertiesUtils;
import com.sgl.hms.order.util.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WeiXinServiceImpl implements WeiXinService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RefundService refundService;

    //下单 生成二维码
    @Override
    public Map createNative(Long orderId) {

        try {

            Map map = (Map) redisTemplate.opsForValue().get(orderId.toString());
            if (null != map){
                return map;
            }

            //1 根据订单id获取订单信息
            OrderInfo orderInfo = orderService.getById(orderId);

            //2 向支付记录表中添加信息(支付类型（1：微信 2：支付宝）)
            paymentService.savePaymentInfo(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());

            //3 设置参数，调用微信生成二维码接口
            //把参数转换成xml格式（微信功能要求），使用商户key进行加密
            Map paramMap = new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());  //微信包自带工具类
            String body = orderInfo.getReserveDate() + "就诊"+ orderInfo.getDepname();
            paramMap.put("body", body);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());//orderInfo.getOutTradeNo()订单交易号，生成订单时随机生成
            //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee", "1");  //测试使用，统一写成这个值，代表1分钱
            paramMap.put("spbill_create_ip", "127.0.0.1");
            paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
            paramMap.put("trade_type", "NATIVE");

            //4 调用微信生成二维码接口，HttpClient调用
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //设置map参数
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap,ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();

            //5 返回相关数据并转换map集合
            String content = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);

            log.info(String.valueOf(resultMap));

           /* {
                 nonce_str=2FrKcPt3V8MJ6ddN,
                 code_url=weixin://wxpay/bizpayurl?pr=0ZMGUHmzz,***
                 appid=wx74862e0dfcf69954,
                 sign=5D329B33D2BEF7A34EC8868C092F8CC4,
                 trade_type=NATIVE,
                 return_msg=OK,
                 result_code=SUCCESS,***
                 mch_id=1558950191,
                 return_code=SUCCESS,
                 prepay_id=wx2222475421153054e6541b23758cf80000
            }*/


                //6 封装返回结果集
            map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", orderInfo.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url"));  //二维码地址

            if (null != resultMap.get("result_code")){
                //微信支付二维码2小时过期，可采取2小时未支付取消订单
                redisTemplate.opsForValue().set(orderId.toString(),map,120, TimeUnit.MICROSECONDS);
            }
            return map;

        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }

    }


    //查询支付状态
    @Override
    public Map<String, String> queryPayStatus(Long orderId) {

        try {
            //1 根据orderId获取订单信息
            OrderInfo orderInfo = orderService.getById(orderId);

            //2 封装提交参数
            Map paramMap = new HashMap<>();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());

            //3 设置请求内容
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap,ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();

            //4 得到微信接口返回的数据
            String content = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
            log.info(String.valueOf(resultMap));

            //5 把数据接口返回
            return resultMap;


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    //退款
    @Override
    public Boolean refund(Long orderId) {

        try{

            //获取支付记录
            PaymentInfo paymentInfo = paymentService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());
            //保存退款记录并返回数据
            RefundInfo refundInfo = refundService.saveRefundInfo(paymentInfo);
            //判断当前订单数据是否已经退款
            if (refundInfo.getRefundStatus().intValue() == RefundStatusEnum.REFUND.getStatus().intValue()){
                return true;
            }
            //调用微信接口
            //封装需要参数
            Map<String,String> paramMap = new HashMap<>(8);
            paramMap.put("appid",ConstantPropertiesUtils.APPID);       //公众账号ID
            paramMap.put("mch_id",ConstantPropertiesUtils.PARTNER);   //商户编号
            paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
            paramMap.put("transaction_id",paymentInfo.getTradeNo()); //微信订单号
            paramMap.put("out_trade_no",paymentInfo.getOutTradeNo()); //商户订单编号
            paramMap.put("out_refund_no","tk"+paymentInfo.getOutTradeNo()); //商户退款单号
            paramMap.put("total_fee","1");
            paramMap.put("refund_fee","1");

            //设置调用接口功能（完善HttpClient里面的内容，写上cert值）
            String paramXml = WXPayUtil.generateSignedXml(paramMap,ConstantPropertiesUtils.PARTNERKEY);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            client.setXmlParam(paramXml);
            client.setHttps(true);
            client.setCert(true);
            client.setCertPassword(ConstantPropertiesUtils.PARTNER);
            //client.post();

            //接受返回的数据
            String xml = client.getContent();
            //Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);

            //if (null != resultMap && WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))) {
            if (WXPayConstants.SUCCESS.equalsIgnoreCase("SUCCESS")) {
                //退款成功，修改数据
                refundInfo.setCallbackTime(new Date());
                //refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundInfo.setTradeNo(paymentInfo.getTradeNo());
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                //refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                refundInfo.setCallbackContent(JSONObject.toJSONString(null));
                refundService.updateById(refundInfo);
                return true;
            }
            return false;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
