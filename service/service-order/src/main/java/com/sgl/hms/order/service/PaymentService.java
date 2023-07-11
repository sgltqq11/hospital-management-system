package com.sgl.hms.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sgl.hms.model.order.OrderInfo;
import com.sgl.hms.model.order.PaymentInfo;

import java.util.Map;

public interface PaymentService extends IService<PaymentInfo> {

    //向支付记录表(hms_order的payment_info表)中添加信息
    void savePaymentInfo(OrderInfo orderInfo, Integer paymentType);

    //更新订单状态(支付成功）
    void paySuccess(String out_trade_no, Integer paymentType,Map<String, String> resultMap);

    /**
     * 获取支付记录
     * @param orderId
     * @param paymentType
     * @return
     */
    PaymentInfo getPaymentInfo(Long orderId, Integer paymentType);

}
