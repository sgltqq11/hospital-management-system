package com.sgl.hms.common.utils;

import com.sgl.hms.common.helper.JwtHelper;

import javax.servlet.http.HttpServletRequest;

//获取当前用户信息工具类
public class AuthContextHolder {

    //获取当前用户id
    public static Long getUserId(HttpServletRequest request){

        //从header中获取token
        String token = request.getHeader("token");
        //Jwt 从token中获取用户id
        Long userId = JwtHelper.getUserId(token);

        return userId;
    }

    //获取当前用户名称
    public static String getUserName(HttpServletRequest request){
        String token = request.getHeader("token");
        String userName = JwtHelper.getUserName(token);
        return userName;
    }
}
