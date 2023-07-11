package com.sgl.hms.hosp.controller;

import com.sgl.hms.hosp.service.HospitalService;
import com.sgl.hms.common.result.Result;
import com.sgl.hms.model.hosp.Hospital;
import com.sgl.hms.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Api(tags = "医院管理")
@RestController
@RequestMapping("/admin/hosp/hospital")
@CrossOrigin //解决跨域问题
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    @ApiOperation(value = "获取医院详情")
    @GetMapping("showHospDetail/{id}")
    public Result showHospDetail(
            @ApiParam(name = "id", value = "医院id", required = true)
            @PathVariable String id) {
        return Result.ok(hospitalService.getHospDetailById(id));
    }

    @ApiOperation(value = "更新上线状态")
    @GetMapping("updateHospStatus/{id}/{status}")
    public Result updateHospStatus(@ApiParam(name = "id", value = "医院id", required = true)
                               @PathVariable("id") String id,
                               @ApiParam(name = "status", value = "状态（0：未上线 1：已上线）", required = true)
                               @PathVariable("status") Integer status){
        hospitalService.updateHospStatus(id,status);
        return Result.ok();
    }

    @ApiOperation(value = "查询医院信息(条件带分页)")
    @GetMapping("/list/{pageNum}/{pageSize}")
    public Result listHospital(
            @ApiParam(name = "pageNum", value = "当前页码", required = true)
            @PathVariable("pageNum") Integer pageNum,
            @ApiParam(name = "pageSize", value = "每页记录数", required = true)
            @PathVariable("pageSize") Integer pageSize,
            @ApiParam(name = "hospitalQueryVo", value = "查询对象", required = false)
            HospitalQueryVo hospitalQueryVo
            ){
        Page<Hospital> page = hospitalService.selectHospPage(pageNum,pageSize,hospitalQueryVo);
        return Result.ok(page);
    }
}
