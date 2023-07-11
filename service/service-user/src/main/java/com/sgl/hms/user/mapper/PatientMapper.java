package com.sgl.hms.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sgl.hms.model.user.Patient;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PatientMapper extends BaseMapper<Patient> {
}
