package com.sgl.hms.hosp.controller;

import com.sgl.hms.hosp.service.ScheduleService;
import com.sgl.hms.common.result.Result;
import com.sgl.hms.model.hosp.Schedule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "排班管理")
@RestController
@RequestMapping("/admin/hosp/schedule")
@CrossOrigin
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    //根据医院编号 和 科室编号 ，查询排班规则数据
    @ApiOperation(value ="查询排班规则数据")
    @GetMapping("getScheduleRule/{pageNum}/{pageSize}/{hoscode}/{depcode}")
    public Result getScheduleRule(@ApiParam("当前页") @PathVariable long pageNum,
                                  @ApiParam("页数") @PathVariable long pageSize,
                                  @ApiParam("医院编码") @PathVariable String hoscode,
                                  @ApiParam("科室编码") @PathVariable String depcode){
        Map<String,Object> map = scheduleService.getRuleSchedule(pageNum,pageSize,hoscode,depcode);
        return Result.ok(map);
    }

    //根据医院编号 、科室编号和工作日期，查询排班详细信息
    @ApiOperation(value = "查询排班详细信息")
    @GetMapping("getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public Result getScheduleDetail( @ApiParam("医院编码") @PathVariable String hoscode,
                                     @ApiParam("科室编码") @PathVariable String depcode,
                                     @ApiParam("工作日期") @PathVariable String workDate) {
        List<Schedule> list = scheduleService.getDetailSchedule(hoscode,depcode,workDate);
        return Result.ok(list);
    }

}
