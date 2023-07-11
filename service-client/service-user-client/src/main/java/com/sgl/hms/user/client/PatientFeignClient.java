package com.sgl.hms.user.client;

import com.sgl.hms.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("service-user")
@Repository
public interface PatientFeignClient {


    //对接service-user模块下PatientFeignController的方法
    /**
     * 根据就诊人id获取就诊人信息
     * @param id
     * @return
     */
    @GetMapping("/api/user/patient/inner/get/{id}")
    public Patient getPatientOrder(@PathVariable("id") Long id);
}
