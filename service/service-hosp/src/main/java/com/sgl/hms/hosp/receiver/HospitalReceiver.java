package com.sgl.hms.hosp.receiver;

import com.sgl.hms.common.service.RabbitService;
import com.sgl.hms.common.util.RabbitMQConst;
import com.sgl.hms.hosp.service.ScheduleService;
import com.sgl.hms.model.hosp.Schedule;
import com.sgl.hms.vo.order.OrderMqVo;
import com.sgl.hms.vo.sms.SmsVo;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;


import java.io.IOException;

@Component
public class HospitalReceiver {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private RabbitService rabbitService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = RabbitMQConst.QUEUE_ORDER, durable = "true"),
            exchange = @Exchange(value = RabbitMQConst.EXCHANGE_DIRECT_ORDER),
            key = {RabbitMQConst.ROUTING_ORDER}
    ))
    public void receiver(OrderMqVo orderMqVo, Message message, Channel channel) throws IOException {


        if(null != orderMqVo.getAvailableNumber()) {
            //下单成功更新预约数
            Schedule schedule = scheduleService.getById(orderMqVo.getScheduleId());
            schedule.setReservedNumber(orderMqVo.getReservedNumber());
            schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
            scheduleService.update(schedule);
        }else {
            //取消预约更新预约数
            Schedule schedule = scheduleService.getById(orderMqVo.getScheduleId());
            int availableNumber = schedule.getAvailableNumber().intValue() + 1;
            schedule.setAvailableNumber(availableNumber);
            scheduleService.update(schedule);
        }
        //发送短信
        SmsVo smsVo = orderMqVo.getSmsVo();
        if(null != smsVo) {
            //mq发送短信，service-sms模块监听并发送短信提示（这里没有短信提示，阿里云未完善，直接返回了true）
            rabbitService.sendMessage(RabbitMQConst.EXCHANGE_DIRECT_SMS, RabbitMQConst.ROUTING_SMS_ITEM, smsVo);
        }
    }
}
