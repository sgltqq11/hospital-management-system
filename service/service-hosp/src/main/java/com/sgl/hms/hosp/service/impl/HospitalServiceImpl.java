package com.sgl.hms.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sgl.hms.cmn.client.DictFeignClient;
import com.sgl.hms.hosp.repository.HospitalRepository;
import com.sgl.hms.hosp.service.HospitalService;
import com.sgl.hms.model.hosp.Hospital;
import com.sgl.hms.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DictFeignClient dictFeignClient;

    /**
     * 上传医院信息
     * @param paramMap
     */
    @Override
    public void save(Map<String, Object> paramMap) {
        //把map集合转换为Hospital对象
        String mapString = JSONObject.toJSONString(paramMap);
        Hospital hospital = JSONObject.parseObject(mapString, Hospital.class);

        //判断是否存在数据(根据医院编号获取医院信息)
        String hoscode = hospital.getHoscode();
        Hospital hospitalExist = hospitalRepository.getHospitalByHoscode(hoscode);

        if (null != hospitalExist){
            hospital.setStatus(hospitalExist.getStatus());
            hospital.setCreateTime(hospitalExist.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }else {
            //0：未上线 1：已上线
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }
    }

    //根据医院编号查询
    @Override
    public Hospital getByHoscode(String hoscode) {
        return hospitalRepository.getHospitalByHoscode(hoscode);
    }

    /**
     * 查询医院信息(条件带分页)
     * @param pageNum
     * @param pageSize
     * @param hospitalQueryVo
     * @return
     */
    @Override
    public Page<Hospital> selectHospPage(Integer pageNum, Integer pageSize, HospitalQueryVo hospitalQueryVo) {
        Pageable pageable = PageRequest.of(pageNum - 1,pageSize);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true);

        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital,Hospital.class);

        Example<Hospital> example = Example.of(hospital,matcher);

        Page<Hospital> pages = hospitalRepository.findAll(example, pageable);

        //获取查询list集合，遍历进行医院等级封装
        pages.getContent().forEach(this::setHospitalHosType);

        return pages;
    }
    //获取查询list集合，遍历进行医院等级封装
    private Hospital setHospitalHosType(Hospital hospital) {
        //获取数据字典名称,根据dictCode和value查询，查出医院等级
        String dictName = dictFeignClient.getName("Hostype", hospital.getHostype());
        //获取数据字典名称,根据value查询，查出省市地区
        String provinceName = dictFeignClient.getName(hospital.getProvinceCode());
        String cityName = dictFeignClient.getName(hospital.getCityCode());
        String districtName = dictFeignClient.getName(hospital.getDistrictCode());

        hospital.getParam().put("dictName",dictName);
        hospital.getParam().put("fullAddress",provinceName+cityName+districtName);
        return hospital;
    }

    //更新上线状态
    @Override
    public void updateHospStatus(String id, Integer status) {
        //根据id查询医院信息
        Hospital hospital = hospitalRepository.findById(id).get();

        if (hospital != null){
            //设置医院信息
            hospital.setStatus(status);
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
        }
    }

    //获取医院详情
    @Override
    public Map<String,Object> getHospDetailById(String id) {
        Map<String, Object> map = new HashMap<>();
        //医院基本信息（包含医院等级）
        Hospital hospital = this.setHospitalHosType(hospitalRepository.findById(id).get());
        map.put("hospital",hospital);
        //单独处理更直观
        map.put("bookingRule", hospital.getBookingRule());
        //不需要重复返回
        hospital.setBookingRule(null);
        return  map;
    }

    //根据医院编号获取医院名称
    @Override
    public String getHospName(String hoscode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        if (hospital != null){
            return hospital.getHosname();
        }
        return null;
    }

    //根据医院名称模糊查询
    @Override
    public List<Hospital> findByHosname(String hosname) {

        List<Hospital> hospitalList = hospitalRepository.findHospitalByHosnameLike(hosname);
        if (!hospitalList.isEmpty()){
            return hospitalList;
        }
        return null;
    }

    //根据医院编号获取医院预约挂号详情
    @Override
    public Map<String, Object> item(String hoscode) {
        Map<String, Object> result = new HashMap<>();
        //医院详情
        Hospital hospital = this.setHospitalHosType(this.getByHoscode(hoscode));
        result.put("hospital", hospital);
        //预约规则
        result.put("bookingRule", hospital.getBookingRule());
        //不需要重复返回
        hospital.setBookingRule(null);
        return result;
    }


}
