package com.sgl.hms.hosp.service;

import com.sgl.hms.model.hosp.Hospital;
import com.sgl.hms.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface HospitalService {
    /**
     * 上传医院信息
     * @param paramMap
     */
    void save(Map<String, Object> paramMap);

    //根据医院编号查询
    Hospital getByHoscode(String hoscode);

    //查询医院信息(条件带分页)
    Page<Hospital> selectHospPage(Integer pageNum, Integer pageSize, HospitalQueryVo hospitalQueryVo);

    //更新上线状态
    void updateHospStatus(String id, Integer status);

    //获取医院详情
    Map<String,Object> getHospDetailById(String id);

    //根据医院编号获取医院名称
    String getHospName(String hoscode);

    //根据医院名称模糊查询
    List<Hospital> findByHosname(String hosname);

    //根据医院编号获取医院预约挂号详情
    Map<String, Object> item(String hoscode);
}
