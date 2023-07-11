package com.sgl.hms.hosp.controller;

import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sgl.hms.common.result.Result;
import com.sgl.hms.common.utils.MD5;
import com.sgl.hms.hosp.service.HospitalSetService;
import com.sgl.hms.model.hosp.HospitalSet;
import com.sgl.hms.vo.hosp.HospitalSetQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@Api(tags = "医院设置管理")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
@CrossOrigin //解决跨域问题
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;

    //http://localhost:8201/admin/hosp/hospitalSet/findAll
    //1 查询医院设置表所有信息
    @ApiOperation(value = "获取医院设置信息")
    @GetMapping("/findAll")
    public Result finAllHospitalSet(){
        //MP的方法
        List<HospitalSet> list = hospitalSetService.list();
        return Result.ok(list);
    }

    //2 根据id删除医院设置
    @ApiOperation(value = "逻辑删除医院设置")
    @DeleteMapping("/{id}")
    public Result removeHospitalSet(@ApiParam("医院设置id") @PathVariable("id") Long id){
        boolean flag = hospitalSetService.removeById(id);
        if (flag) {
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    //3 条件查询带分页
    @ApiOperation(value = "条件查询带分页获取医院设置")
    @PostMapping("/findPageHospSet/{current}/{limit}")
    public Result findPageHospSet(@ApiParam("当前页") @PathVariable("current") long current,
                                  @ApiParam("每条记录数") @PathVariable("limit") long limit,
                                                                    //required = false代表hospitalSetQueryVo可以为空
                                  @ApiParam("Vo:封装医院名称和医院编号") @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo){

        //创建page对象
        Page<HospitalSet> page = new Page(current,limit);
        //构造条件
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper();
        String hosCode = hospitalSetQueryVo.getHoscode();//医院编号
        String hosName = hospitalSetQueryVo.getHosname();//医院名称
        if (!StringUtils.isEmpty(hosCode)){
            queryWrapper.eq("hoscode",hosCode);
        }
        if (!StringUtils.isEmpty(hosName)) {
            queryWrapper.like("hosname",hosName);
        }
        //调用方法实现分页查询
        Page<HospitalSet> hospitalSetPage = hospitalSetService.page(page, queryWrapper);
        //返回结果
        return Result.ok(hospitalSetPage);
    }

    //4 添加医院设置
    @ApiOperation(value = "添加医院设置")
    @PostMapping("/saveHospitalSet")
    public Result saveHospitalSet(@RequestBody HospitalSet hospitalSet){
        //设置状态 1 使用 0 不能使用
        hospitalSet.setStatus(1);
        //签名秘钥
        Random random = new Random();
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis()+""+random.nextInt(1000)));
        //调用方保存法
        boolean flag = hospitalSetService.save(hospitalSet);
        if (flag){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    //5 根据id获取医院设置
    @ApiOperation(value = "根据id获取医院设置")
    @GetMapping("/getHospSet/{id}")
    public Result getHospSet(@ApiParam("医院设置id") @PathVariable("id") Long id){

        //调用方法
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        if (hospitalSet == null){
            return Result.fail();
        }
        return Result.ok(hospitalSet);
    }

    //6 修改医院设置
    @ApiOperation(value = "修改医院设置")
    @PostMapping("/updateHospitalSet")
    public Result updateHospitalSet(@RequestBody HospitalSet hospitalSet) {
        boolean flag = hospitalSetService.updateById(hospitalSet);
        if(flag) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }

    //7 根据id集合批量删除医院设置
    @ApiOperation(value = "根据id集合批量删除医院设置")
    @DeleteMapping("/batchRemove")
    public Result batchRemoveHospitalSet(@ApiParam("医院设置id集合") @RequestBody List<Long> idList) {
        hospitalSetService.removeByIds(idList);
        return Result.ok();
    }

    //8 医院设置锁定和解锁
    @ApiOperation(value = "医院设置锁定和解锁")
    @PutMapping("/lockHospitalSet/{id}/{status}")
    public Result lockHospitalSet(@ApiParam("医院设置id") @PathVariable("id") Long id,
                                  @ApiParam("状态(1:使用 0:不能使用)") @PathVariable("status") Integer status){

        //根据id查询医院设置信息
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        if (hospitalSet == null){
            return Result.fail();
        }
        //设置状态
        hospitalSet.setStatus(status);
        //调用方法
        boolean flag = hospitalSetService.updateById(hospitalSet);
        if (flag){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }

    //9 发送签名密钥
    @ApiOperation(value = "发送签名密钥")
    @PutMapping("/sendKey/{id}")
    public Result sendKey(@PathVariable("id") Long id){
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        String signKey = hospitalSet.getSignKey();
        String hosCode = hospitalSet.getHoscode();
        //TODO 发送短信
        return Result.ok();
    }

}
