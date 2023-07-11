package com.sgl.hms.order.controller.api;

import com.sgl.hms.common.result.Result;
import com.sgl.hms.enums.PaymentTypeEnum;
import com.sgl.hms.order.service.PaymentService;
import com.sgl.hms.order.service.WeiXinService;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/order/weixin")
public class WeixinController {

    @Autowired
    private WeiXinService weiXinService;

    @Autowired
    private PaymentService paymentService;

    /**
     * 下单 生成二维码
     */
    @GetMapping("/createNative/{orderId}")
    public Result createNative(
            @ApiParam(name = "orderId", value = "订单id", required = true)
            @PathVariable("orderId") Long orderId) {
        return Result.ok(weiXinService.createNative(orderId));
    }


    //查询支付状态
    @GetMapping("/queryPayStatus/{orderId}")
    public Result queryPayStatus(
            @ApiParam(name = "orderId", value = "订单id", required = true)
            @PathVariable("orderId") Long orderId) {

        //调用微信接口实现支付状态查询
        Map<String,String> resultMap = weiXinService.queryPayStatus(orderId);

        /*{ 未支付查询状态
            nonce_str=LdNDEjZ9CYEpW0ep,
            device_info=,
            trade_state=NOTPAY,   ****
            out_trade_no=168214351856098,
            appid=wx74862e0dfcf69954,
            total_fee=1,
            trade_state_desc=订单未支付,
            sign=802AEF4C5E3FAB32D3E980B56B9423AD,
            return_msg=OK,
            result_code=SUCCESS,
            mch_id=1558950191,
            return_code=SUCCESS
        }*/

        /*{  支付成功
            transaction_id=4200001847202304223084558076,
            nonce_str=xdLQmKLjCt3b2cV8,
            trade_state=SUCCESS,
            bank_type=OTHERS,
            openid=oHwsHuNmbZni-t7AcU7J6SUrCtns,
            sign=C6B014634D596A64FB28EE0185535D14,
            return_msg=OK,
            fee_type=CNY,
            mch_id=1558950191,
            cash_fee=1,
            out_trade_no=168209170237469,
            cash_fee_type=CNY,
            appid=wx74862e0dfcf69954,
            total_fee=1,
            trade_state_desc=支付成功,
            trade_type=NATIVE,
            result_code=SUCCESS,
            attach=,
            time_end=20230422235454,
            is_subscribe=N,
            return_code=SUCCESS
        }*/

        //判断
        if (null == resultMap) return Result.fail().message("支付出错");

        if ("SUCCESS".equals(resultMap.get("trade_state"))){  //支付成功
            //更新订单状态
            String out_trade_no = resultMap.get("out_trade_no"); //订单编号（订单交易号，生成订单接口存入数据库时随机生成）
            paymentService.paySuccess(out_trade_no, PaymentTypeEnum.WEIXIN.getStatus(),resultMap);
            return Result.ok().message("支付成功");

        }

        return Result.ok().message("支付中");
    }


}
