package com.sgl.hms.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sgl.hms.common.result.Result;
import com.sgl.hms.model.user.UserInfo;
import com.sgl.hms.user.service.UserInfoService;
import com.sgl.hms.vo.user.UserInfoQueryVo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/admin/user")
@Api(tags = "用户列表")
public class UserController {

    @Autowired
    private UserInfoService userInfoService;

    //用户列表（条件查询带分页）
    @ApiOperation(value = "用户列表（条件查询带分页")
    @GetMapping("{current}/{limit}")
    public Result list(@ApiParam(value = "当前页") @PathVariable Long current,
                       @ApiParam(value = "每页记录数") @PathVariable Long limit,
                       UserInfoQueryVo userInfoQueryVo) {
        Page<UserInfo> pageParam = new Page<>(current,limit);
        IPage<UserInfo> page =
                userInfoService.selectPage(pageParam,userInfoQueryVo);
        return Result.ok(page);
    }

    @ApiOperation(value = "用户锁定")
    @GetMapping("lock/{userId}/{status}")
    public Result lock(
            @ApiParam(value = "用户id") @PathVariable("userId") Long userId,
            @ApiParam(value = "用户状态") @PathVariable("status") Integer status){
        userInfoService.lock(userId, status);
        return Result.ok();
    }


    //用户详情
    @ApiOperation(value = "用户详情")
    @GetMapping("show/{userId}")
    public Result show(@ApiParam(value = "用户id") @PathVariable Long userId) {
        Map<String, Object> map = userInfoService.show(userId);
        return Result.ok(map);
    }

    //用户认证审批
    @ApiOperation(value = "用户认证审批")
    @GetMapping("approval/{userId}/{authStatus}")
    public Result approval(@PathVariable Long userId,@PathVariable Integer authStatus) {
        userInfoService.approval(userId,authStatus);
        return Result.ok();
    }
}
