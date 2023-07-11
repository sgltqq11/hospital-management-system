package com.sgl.hms.statistics.controller;

import com.sgl.hms.common.result.Result;
import com.sgl.hms.order.client.OrderFeignClient;
import com.sgl.hms.vo.order.OrderCountQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(tags = "统计管理接口")
@RestController
@CrossOrigin
@RequestMapping("/admin/statistics")
public class StatisticsController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    @ApiOperation(value = "获取订单统计数据")
    @GetMapping("getCountMap")
    public Result getCountMap(@ApiParam(name = "orderCountQueryVo", value = "查询对象", required = false)
                                          OrderCountQueryVo orderCountQueryVo) {

        Map<String, Object> map = orderFeignClient.getCountMap(orderCountQueryVo);

        return Result.ok(map);
    }
}

