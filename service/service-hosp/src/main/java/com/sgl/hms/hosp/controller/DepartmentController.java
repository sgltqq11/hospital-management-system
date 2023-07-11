package com.sgl.hms.hosp.controller;

import com.sgl.hms.hosp.service.DepartmentService;
import com.sgl.hms.common.result.Result;
import com.sgl.hms.vo.hosp.DepartmentVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "科室管理")
@RestController
@RequestMapping("/admin/hosp/department")
@CrossOrigin
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    //根据医院编号，查询医院所有科室列表
    @ApiOperation(value = "查询医院所有科室列表")
    @GetMapping("getDeptList/{hoscode}")
    public Result getDeptList(@ApiParam(name = "hoscode", value = "医院编号", required = true)
                                  @PathVariable String hoscode) {
        List<DepartmentVo> list = departmentService.findDepartmentTree(hoscode);
        return Result.ok(list);
    }
}
