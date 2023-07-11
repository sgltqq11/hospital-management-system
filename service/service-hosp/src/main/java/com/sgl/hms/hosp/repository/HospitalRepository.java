package com.sgl.hms.hosp.repository;

import com.sgl.hms.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRepository extends MongoRepository<Hospital,String> {
    //根据医院编号获取医院信息
    Hospital getHospitalByHoscode(String hoscode);

    //根据医院名称获取医院列表
    List<Hospital> findHospitalByHosnameLike(String hosname);
}
