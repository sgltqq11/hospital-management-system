package com.sgl.hms.task.scheduled;

import com.sgl.hms.common.service.RabbitService;
import com.sgl.hms.common.util.RabbitMQConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling  //表示开启定时任务操作
public class ScheduledTask {

    @Autowired
    private  RabbitService rabbitService;

    /**
     * 每天8点执行 提醒就诊
     * cron表达式 执行间隔
     * 0 0 8 * * ?   表示每天八点执行
     */
    @Scheduled(cron = "0/30 * * * * ?")  //每30秒执行
    public void taskPatient() {

        //发送mq消息，service-order中监听（receiver/OrderReceiver）
        rabbitService.sendMessage(RabbitMQConst.EXCHANGE_DIRECT_TASK, RabbitMQConst.ROUTING_TASK_8, "");
    }
}
