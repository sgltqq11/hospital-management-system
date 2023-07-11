package com.sgl.hms.hosp.repository;

import com.sgl.hms.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends MongoRepository<Department,String> {
    //上传科室信息(根据医院编号 和 科室编号查询)
    Department getDepartmentByHoscodeAndDepcode(String hoscode, String depcode);

}
