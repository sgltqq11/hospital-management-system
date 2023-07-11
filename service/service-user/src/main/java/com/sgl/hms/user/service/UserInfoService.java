package com.sgl.hms.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sgl.hms.model.user.UserInfo;
import com.sgl.hms.vo.user.LoginVo;
import com.sgl.hms.vo.user.UserAuthVo;
import com.sgl.hms.vo.user.UserInfoQueryVo;

import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {

    //手机号登陆
    Map<String, Object> login(LoginVo loginVo);

    //根据openid查询
    UserInfo selectWxInfoOpenId(String openId);

    //用户认证接口
    void userAuth(Long userId, UserAuthVo userAuthVo);

    //用户列表（条件查询带分页）
    IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo);

    //用户锁定
    void lock(Long userId, Integer status);

    //用户详情
    Map<String, Object> show(Long userId);

    //用户认证审批
    void approval(Long userId, Integer authStatus);
}
