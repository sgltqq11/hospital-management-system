package com.sgl.hms.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sgl.hms.enums.RefundStatusEnum;
import com.sgl.hms.model.order.PaymentInfo;
import com.sgl.hms.model.order.RefundInfo;
import com.sgl.hms.order.mapper.RefundInfoMapper;
import com.sgl.hms.order.service.RefundService;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RefundServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundService {


    /**
     * 保存退款记录
     * @param paymentInfo
     */
    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {

        //查询，判断是有数据
        QueryWrapper<RefundInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", paymentInfo.getOrderId());
        queryWrapper.eq("payment_type", paymentInfo.getPaymentType());
        RefundInfo refundInfo = baseMapper.selectOne(queryWrapper);
        //有数据直接返回
        if(null != refundInfo) return refundInfo;

        // 没有数据，保存交易记录并返回
        refundInfo = new RefundInfo();
        refundInfo.setCreateTime(new Date());
        refundInfo.setOrderId(paymentInfo.getOrderId());
        refundInfo.setPaymentType(paymentInfo.getPaymentType());
        refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
        refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());//退款中
        refundInfo.setSubject(paymentInfo.getSubject());

        refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
        baseMapper.insert(refundInfo);
        return refundInfo;
    }
}
