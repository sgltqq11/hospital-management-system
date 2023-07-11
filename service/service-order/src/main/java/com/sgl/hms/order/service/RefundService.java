package com.sgl.hms.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sgl.hms.model.order.PaymentInfo;
import com.sgl.hms.model.order.RefundInfo;

public interface RefundService extends IService<RefundInfo> {

    /**
     * 保存退款记录
     * @param paymentInfo
     */
    RefundInfo saveRefundInfo(PaymentInfo paymentInfo);

}
