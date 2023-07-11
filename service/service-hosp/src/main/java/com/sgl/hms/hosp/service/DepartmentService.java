package com.sgl.hms.hosp.service;

import com.sgl.hms.model.hosp.Department;
import com.sgl.hms.vo.hosp.DepartmentQueryVo;
import com.sgl.hms.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
    //上传科室信息
    void save(Map<String, Object> paramMap);

    //查询科室信息
    Page<Department> getPageDepartment(int pageNum, int pageSize, DepartmentQueryVo departmentQueryVo);

    //删除科室信息
    void removeDepartment(String hoscode, String depcode);

    //根据医院编号，查询医院所有科室列表
    List<DepartmentVo> findDepartmentTree(String hoscode);

    //根据 医院编号 和 科室编号 查询科室名称
    String getDepName(String hoscode, String depcode);

    //根据 医院编号 和 科室编号 查询科室
    Department getDepartment(String hoscode, String depcode);
}
