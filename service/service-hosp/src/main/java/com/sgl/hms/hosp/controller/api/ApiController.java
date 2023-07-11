package com.sgl.hms.hosp.controller.api;

import com.sgl.hms.hosp.service.DepartmentService;
import com.sgl.hms.hosp.service.HospitalService;
import com.sgl.hms.hosp.service.HospitalSetService;
import com.sgl.hms.hosp.service.ScheduleService;
import com.sgl.hms.common.exception.CustomHmsGlobalException;
import com.sgl.hms.common.helper.HttpRequestHelper;
import com.sgl.hms.common.result.Result;
import com.sgl.hms.common.result.ResultCodeEnum;
import com.sgl.hms.common.utils.MD5;
import com.sgl.hms.model.hosp.Department;
import com.sgl.hms.model.hosp.Hospital;
import com.sgl.hms.model.hosp.Schedule;
import com.sgl.hms.vo.hosp.DepartmentQueryVo;
import com.sgl.hms.vo.hosp.ScheduleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(tags = "医院管理API接口")
@RestController
@RequestMapping("/api/hosp")

public class ApiController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;


    @ApiOperation(value = "删除科室信息")
    @PostMapping("/schedule/remove")
    public Result removeSchedule(HttpServletRequest request){

        //获取传递过来的排班信息
        Map<String, String[]> ResultMap = request.getParameterMap();
        //将String[] ---> Object
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(ResultMap);

        //医院编号
        String hoscode = (String) paramMap.get("hoscode");
        //排班编号
        String hosScheduleId = (String) paramMap.get("hosScheduleId");

        //签名校验
        String hospSign = (String) paramMap.get("sign");

        //2.根据传递过来的医院编号，查询数据库，查询签名
        String signKey = hospitalSetService.getSignKey(hoscode);

        //3.进行MD5加密
        signKey = MD5.encrypt(signKey);

        //4.判断签名是否一致
        if (!signKey.equals(hospSign)){
            throw new CustomHmsGlobalException(ResultCodeEnum.SIGN_ERROR);
        }

        //调用service方法
        scheduleService.removeSchedule(hoscode,hosScheduleId);
        return Result.ok();
    }

    @ApiOperation(value = "查询排班信息")
    @PostMapping("/schedule/list")
    public Result getschedule(HttpServletRequest request){

        //获取传递过来的排班信息
        Map<String, String[]> ResultMap = request.getParameterMap();
        //将String[] ---> Object
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(ResultMap);

        //医院编号
        String hoscode = (String) paramMap.get("hoscode");

        //当前页 和 每页记录数
        int pageNum = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
        int pageSize = StringUtils.isEmpty(paramMap.get("limit")) ? 3 : Integer.parseInt((String) paramMap.get("limit"));

        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);

        //签名校验
        String hospSign = (String) paramMap.get("sign");

        //2.根据传递过来的医院编号，查询数据库，查询签名
        String signKey = hospitalSetService.getSignKey(hoscode);

        //3.进行MD5加密
        signKey = MD5.encrypt(signKey);

        //4.判断签名是否一致
        if (!signKey.equals(hospSign)){
            throw new CustomHmsGlobalException(ResultCodeEnum.SIGN_ERROR);
        }

        //调用service方法
        Page<Schedule> page = scheduleService.getPageSchedule(pageNum,pageSize,scheduleQueryVo);
        return Result.ok(page);
    }

    @ApiOperation(value = "上传排班信息接口")
    @PostMapping("/saveSchedule")
    public Result saveSchedule(HttpServletRequest request){

        //获取传递过来的科室信息
        Map<String, String[]> ResultMap = request.getParameterMap();
        //将String[] ---> Object
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(ResultMap);

        //1.获取医院系统传递过来的签名，签名进行MD5加密
        String hospSign = (String) paramMap.get("sign");

        //2.根据传递过来的医院编号，查询数据库，查询签名
        String hoscode = (String) paramMap.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);

        //3.进行MD5加密
        signKey = MD5.encrypt(signKey);

        //4.判断签名是否一致
        if (!signKey.equals(hospSign)){
            throw new CustomHmsGlobalException(ResultCodeEnum.SIGN_ERROR);
        }

        //调用service方法
        scheduleService.save(paramMap);

        return Result.ok();
    }

    @ApiOperation(value = "删除科室信息")
    @PostMapping("/department/remove")
    public Result removeDepartment(HttpServletRequest request){

        //获取传递过来的科室信息
        Map<String, String[]> ResultMap = request.getParameterMap();
        //将String[] ---> Object
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(ResultMap);

        //医院编号
        String hoscode = (String) paramMap.get("hoscode");
        //科室编号
        String depcode = (String) paramMap.get("depcode");

        //签名校验
        String hospSign = (String) paramMap.get("sign");

        //2.根据传递过来的医院编号，查询数据库，查询签名
        String signKey = hospitalSetService.getSignKey(hoscode);

        //3.进行MD5加密
        signKey = MD5.encrypt(signKey);

        //4.判断签名是否一致
        if (!signKey.equals(hospSign)){
            throw new CustomHmsGlobalException(ResultCodeEnum.SIGN_ERROR);
        }

        //调用service方法
        departmentService.removeDepartment(hoscode,depcode);
        return Result.ok();
    }

    @ApiOperation(value = "查询科室信息")
    @PostMapping("/department/list")
    public Result getDepartment(HttpServletRequest request){

        //获取传递过来的科室信息
        Map<String, String[]> ResultMap = request.getParameterMap();
        //将String[] ---> Object
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(ResultMap);

        //医院编号
        String hoscode = (String) paramMap.get("hoscode");

        //当前页 和 每页记录数
        int pageNum = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
        int pageSize = StringUtils.isEmpty(paramMap.get("limit")) ? 3 : Integer.parseInt((String) paramMap.get("limit"));

        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);

        //签名校验
        String hospSign = (String) paramMap.get("sign");

        //2.根据传递过来的医院编号，查询数据库，查询签名
        String signKey = hospitalSetService.getSignKey(hoscode);

        //3.进行MD5加密
        signKey = MD5.encrypt(signKey);

        //4.判断签名是否一致
        if (!signKey.equals(hospSign)){
            throw new CustomHmsGlobalException(ResultCodeEnum.SIGN_ERROR);
        }

        //调用service方法
        Page<Department> page = departmentService.getPageDepartment(pageNum,pageSize,departmentQueryVo);
        return Result.ok(page);
    }

    @ApiOperation(value = "上传科室信息接口")
    @PostMapping("/saveDepartment")
    public Result saveDepartment(HttpServletRequest request){

        //获取传递过来的科室信息
        Map<String, String[]> ResultMap = request.getParameterMap();
        //将String[] ---> Object
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(ResultMap);

        //1.获取医院系统传递过来的签名，签名进行MD5加密
        String hospSign = (String) paramMap.get("sign");

        //2.根据传递过来的医院编号，查询数据库，查询签名
        String hoscode = (String) paramMap.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);

        //3.进行MD5加密
        signKey = MD5.encrypt(signKey);

        //4.判断签名是否一致
        if (!signKey.equals(hospSign)){
            throw new CustomHmsGlobalException(ResultCodeEnum.SIGN_ERROR);
        }

        //调用service方法
        departmentService.save(paramMap);

        return Result.ok();
    }


    @ApiOperation(value = "查询医院接口")
    @PostMapping("hospital/show")
    public Result getHospital(HttpServletRequest request){

        //获取传递过来的医院信息
        Map<String, String[]> ResultMap = request.getParameterMap();
        //将String[] ---> Object
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(ResultMap);
        //获取医院编号
        String hoscode = (String) paramMap.get("hoscode");

        //1.获取医院系统传递过来的签名，签名进行MD5加密
        String hospSign = (String) paramMap.get("sign");

        //2.根据传递过来的医院编号，查询数据库，查询签名
        String signKey = hospitalSetService.getSignKey(hoscode);

        //3.进行MD5加密
        signKey = MD5.encrypt(signKey);

        //4.判断签名是否一致
        if (!signKey.equals(hospSign)){
            throw new CustomHmsGlobalException(ResultCodeEnum.SIGN_ERROR);
        }

        //调用service方法根据医院编号查询
        Hospital hospital = hospitalService.getByHoscode(hoscode);

        return Result.ok(hospital);
    }


    @ApiOperation(value = "上传医院接口")
    @PostMapping("/saveHospital")
    public Result savaHospital(HttpServletRequest request){

        //获取传递过来的医院信息
        Map<String, String[]> ResultMap = request.getParameterMap();
        //将String[] ---> Object
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(ResultMap);

        //1.获取医院系统传递过来的签名，签名进行MD5加密(从数据库hms_manage查表hospital_set（hospital-manage模块）)
        String hospSign = (String) paramMap.get("sign");

        //2.根据传递过来的医院编号，查询数据库，查询签名
        String hoscode = (String) paramMap.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode); //从当前接口查hms_hosp查表hospital_set

        //3.进行MD5加密
        signKey = MD5.encrypt(signKey);

        //4.判断签名是否一致
        if (!signKey.equals(hospSign)){
            throw new CustomHmsGlobalException(ResultCodeEnum.SIGN_ERROR);
        }

        //传输过程中 “+” 转换为 “ ” ，因此需要转换回来
        String logoData = (String) paramMap.get("logoData");
        logoData = logoData.replaceAll(" ","+");
        paramMap.put("logoData",logoData);

        //调用service方法
        hospitalService.save(paramMap);

        return Result.ok();
    }

}
