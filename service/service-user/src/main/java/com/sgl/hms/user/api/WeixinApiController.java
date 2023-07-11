package com.sgl.hms.user.api;


import com.alibaba.fastjson.JSONObject;
import com.sgl.hms.common.exception.CustomHmsGlobalException;
import com.sgl.hms.common.helper.JwtHelper;
import com.sgl.hms.common.result.Result;
import com.sgl.hms.common.result.ResultCodeEnum;
import com.sgl.hms.model.user.UserInfo;
import com.sgl.hms.user.service.UserInfoService;
import com.sgl.hms.user.util.ConstantPropertiesUtil;
import com.sgl.hms.user.util.HttpClientUtils;
import com.sun.deploy.net.URLEncoder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "微信登陆接口")
@Controller
@CrossOrigin
@Slf4j
@RequestMapping("/api/user/wx")
public class WeixinApiController {

    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取微信登录参数和生成微信扫描二维码
     */
    @ApiOperation(value = "获取微信登录参数")
    @GetMapping("getLoginParam")
    @ResponseBody
    public Result genQrConnect(HttpSession session) throws UnsupportedEncodingException {
        String redirectUri = URLEncoder.encode(ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL, "UTF-8");

        Map<String, Object> map = new HashMap<>();
        map.put("appid", ConstantPropertiesUtil.WX_OPEN_APP_ID);
        map.put("redirectUri", redirectUri);
        map.put("scope", "snsapi_login");
        map.put("state", System.currentTimeMillis()+"");//System.currentTimeMillis()+""
        return Result.ok(map);
    }

    /**
     * 微信登录回调
     *
     * @param code
     * @param state
     * @return
     */
    @ApiOperation("微信登录回调")
    @RequestMapping("callback")
    public String callback(String code, String state) {

        //第一步 获取授权临时票据
        log.info("code = " + code);

        if (StringUtils.isEmpty(state) || StringUtils.isEmpty(code)) {
            log.error("非法回调请求");
            throw new CustomHmsGlobalException(ResultCodeEnum.ILLEGAL_CALLBACK_REQUEST_ERROR);
        }

        //第二步 使用code和appid以及appscrect换取access_token 和 openid
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");

        //设置appid、secret和code参数
        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                ConstantPropertiesUtil.WX_OPEN_APP_ID,
                ConstantPropertiesUtil.WX_OPEN_APP_SECRET,
                code);


        String accessTokenInfo = null;
        try {
            //使用HttpClientUtils请求这个地址（get请求）
            accessTokenInfo = HttpClientUtils.get(accessTokenUrl);
            log.info("accessTokenInfo = " + accessTokenInfo);
            /*
            accessTokenInfo = {
                    "access_token":"67_3huEBGOnW_sauKAbpHIu9a20NjDOi52K43xPFta3LlUuEnX39-6JWwyk4dE8XpEK2dMqVW-_1ueFJ1KnPD0XBeIYCWIKFeDaTRxR8KzgCCg",
                    "expires_in":7200,
                    "refresh_token":"67_Sbs9kg4pYrSkaIZHVxo5VMhdLote8UtQiyhPCVp93L9FD_OvdYY7Il8K5zEZpyATMeGPEj3ItUgyhDipG7YSi6WKWkcTEfdXk84OmOkgvig",
                    "openid":"o3_SC533euMiqxnya0_jdQZcgwQI",
                    "scope":"snsapi_login",
                    "unionid":"oWgGz1JpqX-NZXo4pNRqP5Ek0k0A"
            }
             */
        } catch (Exception e) {
            throw new CustomHmsGlobalException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }

        //校验 和 从返回的字符串中获取两个值 openid 和 access_token
        JSONObject jsonObject = JSONObject.parseObject(accessTokenInfo);
        if(null != jsonObject.getString("errcode")){
            log.error("获取access_token失败：" + jsonObject.getString("errcode") + jsonObject.getString("errmsg"));
            throw new CustomHmsGlobalException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }
        String access_token = jsonObject.getString("access_token");
        String openId = jsonObject.getString("openid");

        //判断数据库是否存在微信扫描人信息，根据openid判断
        UserInfo userInfo = userInfoService.selectWxInfoOpenId(openId);

        if (userInfo == null){  //数据库不存在微信信息
            //第三步 根据openid和access_token请求微信地址 得到扫描人微信信息
            String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                    "?access_token=%s" +
                    "&openId=%s";
            String userInfoUrl = String.format(baseUserInfoUrl, access_token, openId);

            String userInfoResult = null;
            try {
                userInfoResult = HttpClientUtils.get(userInfoUrl);
                log.info("userInfoResult = " + userInfoResult);
            /*
            userInfoResult = {
            "openid":"o3_SC533euMiqxnya0_jdQZcgwQI",
            "nickname":"彳亍（chi chu）",
            "sex":0,
            "language":"",
            "city":"",
            "province":"",
            "country":"",
            "headimgurl":"https:\/\/thirdwx.qlogo.cn\/mmopen\/vi_32\/ykGRcou2Pxkl895kLYBnLiaam4jgmxM5GxPOZXYZqSf0eUNugdxvpkOJcwjU2S063evrnGsx3NFpT5iaOYiaIpetA\/132",
            "privilege":[],
            "unionid":"oWgGz1JpqX-NZXo4pNRqP5Ek0k0A"}
             */
            } catch (Exception e) {
                throw new CustomHmsGlobalException(ResultCodeEnum.FETCH_USERINFO_ERROR);
            }

            JSONObject userInfoResultJson = JSONObject.parseObject(userInfoResult);
            if(null != userInfoResultJson.getString("errcode")){
                log.error("获取access_token失败：" + userInfoResultJson.getString("errcode") + userInfoResultJson.getString("errmsg"));
                throw new CustomHmsGlobalException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
            }

            //第四步 微信扫描人信息添加到数据库
            //解析用户信息
            //用户昵称
            String nickname = userInfoResultJson.getString("nickname");
            //用户头像
            String headimgurl = userInfoResultJson.getString("headimgurl");

            userInfo = new UserInfo();
            userInfo.setOpenid(openId);
            userInfo.setNickName(nickname);
            userInfo.setStatus(1);
            userInfoService.save(userInfo);
        }

        //返回name和token字符串
        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);


        //判断userInfo是否有手机号，如果手机号为空，返回openid
        //如果手机号不为空，返回openid值是空字符串
        //前端判断：如果openid不为空，绑定手机号，如果openid为空，不需要绑定手机号
        if(StringUtils.isEmpty(userInfo.getPhone())) {
            map.put("openid", userInfo.getOpenid());
        } else {
            map.put("openid", "");
        }
        //使用jwt生成token字符串
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);
        try {
            //跳转到前端页面
            return "redirect:" + ConstantPropertiesUtil.HMS_BASE_URL + "/weixin/callback?token="+map.get("token")+
                    "&openid="+map.get("openid")+"&name="+URLEncoder.encode((String)map.get("name"),"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

    }
}
