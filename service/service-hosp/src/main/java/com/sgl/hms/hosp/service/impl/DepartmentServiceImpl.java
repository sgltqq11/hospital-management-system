package com.sgl.hms.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sgl.hms.hosp.repository.DepartmentRepository;
import com.sgl.hms.hosp.service.DepartmentService;
import com.sgl.hms.common.exception.CustomHmsGlobalException;
import com.sgl.hms.common.result.ResultCodeEnum;
import com.sgl.hms.model.hosp.Department;
import com.sgl.hms.vo.hosp.DepartmentQueryVo;
import com.sgl.hms.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * 上传科室信息
     * @param paramMap
     */
    @Override
    public void save(Map<String, Object> paramMap) {
        //把map集合转换为Department对象
        String paramMapString = JSONObject.toJSONString(paramMap);
        Department department = JSONObject.parseObject(paramMapString, Department.class);

        //根据医院编号 和 科室编号查询
        Department  departmentExist =
                departmentRepository.getDepartmentByHoscodeAndDepcode(department.getHoscode(),department.getDepcode());

        //判断
        if (null != departmentExist){
            departmentExist.setUpdateTime(new Date());
            departmentExist.setIsDeleted(0);
            departmentRepository.save(departmentExist);
        }else {
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }
    }

    /**
     * 查询科室信息
     * @param pageNum
     * @param pageSize
     * @param departmentQueryVo
     * @return
     */
    @Override
    public Page<Department> getPageDepartment(int pageNum, int pageSize, DepartmentQueryVo departmentQueryVo) {

        //创建Pageable对象
        Pageable pageable = PageRequest.of(pageNum - 1,pageSize);
        //创建Example对象
        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo,department,Department.class);
        department.setIsDeleted(0);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Example<Department> example = Example.of(department,matcher);

        //Page<Department> departmentPage = departmentRepository.findAll(example, pageable);
        return departmentRepository.findAll(example, pageable);
    }

    /**
     * 删除科室信息
     * @param hoscode
     * @param depcode
     */
    @Override
    public void removeDepartment(String hoscode, String depcode) {
        //根据医院编号 和 科室编号
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (department != null){
            departmentRepository.deleteById(department.getId());
        }
    }

    /**
     * 根据医院编号，查询医院所有科室列表
     * @param hoscode
     * @return
     */
    @Override
    public List<DepartmentVo> findDepartmentTree(String hoscode) {
        //创建list集合，用于封装最终数据
        List<DepartmentVo> result = new ArrayList<>();

        //根据医院编号，查询医院所有科室列表
        Department departmentAll = new Department();
        departmentAll.setHoscode(hoscode);
        Example<Department> example = Example.of(departmentAll);
        //所有的科室信息
        List<Department> departmentList = departmentRepository.findAll(example);
        if (departmentList.isEmpty()) throw new CustomHmsGlobalException(ResultCodeEnum.DATA_NULL);

        //根据大科室编号 bigcode 分组，获取每个大科室里面下级子科室
        Map<String, List<Department>> departmentMap =
                departmentList.stream().collect(Collectors.groupingBy(Department::getBigcode));

        //遍历map集合 departmentMap
        for (Map.Entry<String, List<Department>> entry : departmentMap.entrySet()) {
            //大科室编号
            String bigcode = entry.getKey();
            //大科室编号对应的全局数据
            List<Department> departmentListBig = entry.getValue();

            //封装大科室
            DepartmentVo departmentVoBig = new DepartmentVo();
            //设置bigcode
            departmentVoBig.setDepcode(bigcode);
            //设置depname
            departmentVoBig.setDepname(departmentListBig.get(0).getBigname());

            //封装小科室
            List<DepartmentVo> childListSmall = new ArrayList<>();
            //遍历departmentListBig
            for (Department department : departmentListBig) {
                DepartmentVo departmentVoSmall = new DepartmentVo();
                departmentVoSmall.setDepcode(department.getDepcode());
                departmentVoSmall.setDepname(department.getDepname());
                //封装到list集合
                childListSmall.add(departmentVoSmall);
            }
            //把小科室list集合放到大科室children里面（设置children)
            departmentVoBig.setChildren(childListSmall);
            //放到最终集合result中
            result.add(departmentVoBig);
        }
        return result;
    }

    /**
     * 根据 医院编号 和 科室编号 查询科室名称
     * @param hoscode
     * @param depcode
     * @return
     */
    @Override
    public String getDepName(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (department != null){
            return department.getDepname();
        }
        return null;
    }

    //根据 医院编号 和 科室编号 查询科室
    @Override
    public Department getDepartment(String hoscode, String depcode) {

        return departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
    }
}
