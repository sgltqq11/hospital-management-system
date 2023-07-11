package com.sgl.hms.sms.controller;

import com.sgl.hms.common.result.Result;
import com.sgl.hms.sms.service.SmsService;
import com.sgl.hms.sms.utils.RandomUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/sms")
@Api(tags = "短信")
@CrossOrigin
@Slf4j
public class SmsApiController {

    @Autowired
    private SmsService msmService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    //发送手机验证码
    @ApiOperation(value = "发送短信验证码")
    @GetMapping("send/{phone}")
    public Result sendCode(@ApiParam("手机号") @PathVariable String phone) {
        //从redis获取验证码，如果获取获取到，返回ok
        // key 手机号  value 验证码
        String code = redisTemplate.opsForValue().get(phone);
        if(!StringUtils.isEmpty(code)) {
            return Result.ok();
        }
        //如果从redis获取不到，
        // 生成验证码，
        code = RandomUtil.getSixBitRandom();

        //生成验证码放到redis里面，设置有效时间
        redisTemplate.opsForValue().set(phone,code,2, TimeUnit.MINUTES);

        log.info("验证码："+code);

        return Result.ok();

        /*//调用service方法，通过整合短信服务进行发送
        boolean isSend = msmService.send(phone,code);
        //生成验证码放到redis里面，设置有效时间
        if(isSend) {
            redisTemplate.opsForValue().set(phone,code,2, TimeUnit.MINUTES);
            return Result.ok();
        } else {
            return Result.fail().message("发送短信失败");
        }*/
    }
}
