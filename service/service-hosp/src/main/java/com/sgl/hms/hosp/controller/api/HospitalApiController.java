package com.sgl.hms.hosp.controller.api;

import com.sgl.hms.hosp.service.DepartmentService;
import com.sgl.hms.hosp.service.HospitalService;
import com.sgl.hms.common.result.Result;
import com.sgl.hms.hosp.service.HospitalSetService;
import com.sgl.hms.hosp.service.ScheduleService;
import com.sgl.hms.model.hosp.Hospital;
import com.sgl.hms.model.hosp.Schedule;
import com.sgl.hms.vo.hosp.DepartmentVo;
import com.sgl.hms.vo.hosp.HospitalQueryVo;
import com.sgl.hms.vo.hosp.ScheduleOrderVo;
import com.sgl.hms.vo.order.SignInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "医院管理接口")
@RestController
@RequestMapping("/api/hosp/hospital")
@CrossOrigin
public class HospitalApiController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation(value = "获取医院分页列表")
    @GetMapping("findHospList/{pageNum}/{pageSize}")
    public Result findHospList(
            @ApiParam(name = "pageNum", value = "当前页码", required = true)
            @PathVariable("pageNum") Integer pageNum,
            @ApiParam(name = "pageSize", value = "每页记录数", required = true)
            @PathVariable("pageSize") Integer pageSize,
            @ApiParam(name = "hospitalQueryVo", value = "查询对象", required = false)
                    HospitalQueryVo hospitalQueryVo) {
        //显示上线的医院
        Page<Hospital> pageModel = hospitalService.selectHospPage(pageNum, pageSize, hospitalQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "根据医院名称模糊查询")
    @GetMapping("findByHosname/{hosname}")
    public Result findByHosname(
            @ApiParam(name = "hosname", value = "医院名称", required = true)
            @PathVariable String hosname) {
        List<Hospital> hospitalList = hospitalService.findByHosname(hosname);
        return Result.ok(hospitalList);
    }

    @ApiOperation(value = "根据医院编号获取科室列表")
    @GetMapping("department/{hoscode}")
    public Result index(
            @ApiParam(name = "hoscode", value = "医院编号", required = true)
            @PathVariable String hoscode) {

        List<DepartmentVo> list = departmentService.findDepartmentTree(hoscode);

        return Result.ok(list);
    }

    @ApiOperation(value = "根据医院编号获取医院预约挂号详情")
    @GetMapping("findHospDetail/{hoscode}")
    public Result item(
            @ApiParam(name = "hoscode", value = "医院编号", required = true)
            @PathVariable String hoscode) {

        Map<String, Object> map = hospitalService.item(hoscode);

        return Result.ok(map);
    }

    @ApiOperation(value = "获取可预约排班数据")
    @GetMapping("auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getBookingSchedule(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Integer page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Integer limit,
            @ApiParam(name = "hoscode", value = "医院code", required = true)
            @PathVariable String hoscode,
            @ApiParam(name = "depcode", value = "科室code", required = true)
            @PathVariable String depcode) {

        Map<String,Object> map = scheduleService.getBookingScheduleRule(page, limit, hoscode, depcode);

        return Result.ok(map);
    }

    @ApiOperation(value = "获取排班数据")
    @GetMapping("auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public Result findScheduleList(
            @ApiParam(name = "hoscode", value = "医院code", required = true)
            @PathVariable String hoscode,
            @ApiParam(name = "depcode", value = "科室code", required = true)
            @PathVariable String depcode,
            @ApiParam(name = "workDate", value = "排班日期", required = true)
            @PathVariable String workDate) {

        List<Schedule> scheduleList = scheduleService.getDetailSchedule(hoscode, depcode, workDate);

        return Result.ok(scheduleList);
    }

    @ApiOperation(value = "根据排班id获取排班数据")
    @GetMapping("getSchedule/{scheduleId}")
    public Result getSchedule(
            @ApiParam(name = "scheduleId", value = "排班id", required = true)
            @PathVariable String scheduleId) {
        return Result.ok(scheduleService.getById(scheduleId));
    }

    //service-order模块远程调用(通过service-hosp-client模块）
    @ApiOperation(value = "根据排班id获取预约下单数据")
    @GetMapping("inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(
            @ApiParam(name = "scheduleId", value = "排班id", required = true)
            @PathVariable("scheduleId") String scheduleId) {
        return scheduleService.getScheduleOrderVo(scheduleId);
    }

    //service-order模块远程调用
    @ApiOperation(value = "获取医院签名信息")
    @GetMapping("inner/getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(
            @ApiParam(name = "hoscode", value = "医院code", required = true)
            @PathVariable("hoscode") String hoscode) {
        return hospitalSetService.getSignInfoVo(hoscode);
    }
}
