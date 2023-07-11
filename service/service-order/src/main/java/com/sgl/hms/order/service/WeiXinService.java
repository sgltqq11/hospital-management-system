package com.sgl.hms.order.service;

import java.util.Map;

public interface WeiXinService {

    //下单 生成二维码
    Map createNative(Long orderId);

    //查询支付状态
    Map<String, String> queryPayStatus(Long orderId);


    /***
     * 微信退款
     * @param orderId
     * @return
     */
    Boolean refund(Long orderId);

}
