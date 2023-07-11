package com.sgl.hms.user.api;


import com.sgl.hms.common.result.Result;
import com.sgl.hms.common.utils.AuthContextHolder;
import com.sgl.hms.model.user.UserInfo;
import com.sgl.hms.user.service.UserInfoService;
import com.sgl.hms.vo.user.LoginVo;
import com.sgl.hms.vo.user.UserAuthVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/user")
@Api(tags = "用户管理")
public class UserInfoApiController {

    @Autowired
    private UserInfoService userInfoService;


    @ApiOperation(value = "手机号登陆")
    @PostMapping("login")
    public Result login(@ApiParam("登陆实例") @RequestBody LoginVo loginVo, HttpServletRequest request) {

        //loginVo.setIp(IpUtil.getIpAddr(request));
        Map<String, Object> info = userInfoService.login(loginVo);
        return Result.ok(info);

    }

    @ApiOperation(value = "用户认证接口")
    @PostMapping("auth/userAuth")
    public Result userAuth(@ApiParam("认证实例") @RequestBody UserAuthVo userAuthVo, HttpServletRequest request) {
        //传递两个参数，第一个参数用户id，第二个参数认证数据vo对象
        userInfoService.userAuth(AuthContextHolder.getUserId(request),userAuthVo);
        return Result.ok();
    }

    //获取用户id信息接口
    @ApiOperation(value = "获取用户id信息接口")
    @GetMapping("auth/getUserInfo")
    public Result getUserInfo(HttpServletRequest request) {
        Long userId = AuthContextHolder.getUserId(request);
        UserInfo userInfo = userInfoService.getById(userId);
        return Result.ok(userInfo);
    }
}
