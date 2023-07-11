package com.sgl.hms.order.client;

import com.sgl.hms.vo.order.OrderCountQueryVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient("service-order")
@Repository
public interface OrderFeignClient {


    /**
     * 获取订单统计数据
     * @param orderCountQueryVo
     * @return
     */
    @PostMapping("/admin/order/orderInfo/inner/getCountMap")
    public Map<String, Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo);

}
