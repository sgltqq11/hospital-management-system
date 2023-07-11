package com.sgl.hms.hosp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sgl.hms.model.hosp.HospitalSet;
import com.sgl.hms.vo.order.SignInfoVo;

public interface HospitalSetService extends IService<HospitalSet> {

    //根据传递过来的医院编号，查询数据库，查询签名
    String getSignKey(String hoscode);

    //获取医院签名信息
    SignInfoVo getSignInfoVo(String hoscode);
}
