package com.sgl.hms.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sgl.hms.common.result.Result;
import com.sgl.hms.enums.OrderStatusEnum;
import com.sgl.hms.model.order.OrderInfo;
import com.sgl.hms.order.service.OrderService;
import com.sgl.hms.vo.order.OrderCountQueryVo;
import com.sgl.hms.vo.order.OrderQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Api(tags = "订单接口")
@RestController
@RequestMapping("/admin/order/orderInfo")
@CrossOrigin
public class OrderController {

    @Autowired
    private OrderService orderService;

    @ApiOperation(value = "获取订单详情")
    @GetMapping("show/{id}")
    public Result get(
            @ApiParam(name = "orderId", value = "订单id", required = true)
            @PathVariable Long id) {
        return Result.ok(orderService.show(id));
    }

    @ApiOperation(value = "获取分页列表")
    @GetMapping("{page}/{limit}")
    public Result index(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit,
            @ApiParam(name = "orderCountQueryVo", value = "查询对象", required = false) OrderQueryVo orderQueryVo) {
        Page<OrderInfo> pageParam = new Page<>(page, limit);
        IPage<OrderInfo> pageModel = orderService.selectPage(pageParam, orderQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "获取订单状态")
    @GetMapping("getStatusList")
    public Result getStatusList() {
        return Result.ok(OrderStatusEnum.getStatusList());
    }



    @ApiOperation(value = "获取订单统计数据")
    @PostMapping("inner/getCountMap")
    public Map<String, Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo) {

        Map<String, Object> map = orderService.getCountMap(orderCountQueryVo);

        return map;
    }



}
