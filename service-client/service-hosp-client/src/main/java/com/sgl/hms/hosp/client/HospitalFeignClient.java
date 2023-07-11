package com.sgl.hms.hosp.client;

import com.sgl.hms.vo.hosp.ScheduleOrderVo;
import com.sgl.hms.vo.order.SignInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("service-hosp")
@Repository
public interface HospitalFeignClient {

    //对接service-hosp模块下HospitalApiController的两个方法

    /**
     * 根据排班id获取预约下单数据
     * @param scheduleId
     * @return
     */
    @GetMapping("/api/hosp/hospital/inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId);

    /**
     * 获取医院签名信息
     * @param hoscode
     * @return
     */
    @GetMapping("/api/hosp/hospital/inner/getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(@PathVariable("hoscode") String hoscode);

}
