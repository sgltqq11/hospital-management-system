package com.sgl.hms.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sgl.hms.model.order.OrderInfo;
import com.sgl.hms.vo.order.OrderCountQueryVo;
import com.sgl.hms.vo.order.OrderQueryVo;

import java.util.Map;

public interface OrderService extends IService<OrderInfo> {
    //创建订单
    Long saveOrder(String scheduleId, Long patientId);


    //根据订单id查询订单详情
    OrderInfo getOrderInfo(String orderId);

    //订单列表（条件查询带分页）
    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo);


    /**
     * 订单详情
     * @param orderId
     * @return
     */
    Map<String,Object> show(Long orderId);

    //取消预约
    Boolean cancelOrder(Long orderId);

    //就医提醒（就诊通知）
    void patientTips();


    //获取订单统计数据
    Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo);

}
