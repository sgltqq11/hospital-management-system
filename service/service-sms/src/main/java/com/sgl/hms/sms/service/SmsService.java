package com.sgl.hms.sms.service;

import com.sgl.hms.vo.sms.SmsVo;

public interface SmsService {

    //发送手机验证码
    //boolean send(String phone, String code);

    //mq使用发送短信
    boolean send(SmsVo smsVo);
}
