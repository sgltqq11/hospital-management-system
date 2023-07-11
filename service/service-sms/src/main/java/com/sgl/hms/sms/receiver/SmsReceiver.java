package com.sgl.hms.sms.receiver;

import com.sgl.hms.common.util.RabbitMQConst;
import com.sgl.hms.vo.sms.SmsVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.sgl.hms.sms.service.SmsService;

@Component
public class SmsReceiver {

    @Autowired
    private SmsService smsService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = RabbitMQConst.QUEUE_SMS_ITEM, durable = "true"),
            exchange = @Exchange(value = RabbitMQConst.EXCHANGE_DIRECT_SMS),
            key = {RabbitMQConst.ROUTING_SMS_ITEM}
    ))
    public void send(SmsVo smsVo, Message message, Channel channel) {
        smsService.send(smsVo);
    }
}
