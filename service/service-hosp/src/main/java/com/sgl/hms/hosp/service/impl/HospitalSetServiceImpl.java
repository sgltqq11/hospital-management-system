package com.sgl.hms.hosp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sgl.hms.common.exception.CustomHmsGlobalException;
import com.sgl.hms.common.result.ResultCodeEnum;
import com.sgl.hms.hosp.mapper.HospitalSetMapper;
import com.sgl.hms.hosp.service.HospitalSetService;
import com.sgl.hms.model.hosp.HospitalSet;
import com.sgl.hms.vo.order.SignInfoVo;
import org.springframework.stereotype.Service;

@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper,HospitalSet> implements HospitalSetService {

    //根据传递过来的医院编号，查询数据库，查询签名
    @Override
    public String getSignKey(String hoscode) {
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("hoscode",hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(queryWrapper);
        return hospitalSet.getSignKey();
    }

    //获取医院签名信息
    @Override
    public SignInfoVo getSignInfoVo(String hoscode) {
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        wrapper.eq("hoscode",hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);
        if(null == hospitalSet) {
            throw new CustomHmsGlobalException(ResultCodeEnum.HOSPITAL_OPEN);
        }
        SignInfoVo signInfoVo = new SignInfoVo();
        signInfoVo.setApiUrl(hospitalSet.getApiUrl());
        signInfoVo.setSignKey(hospitalSet.getSignKey());
        return signInfoVo;
    }
}
