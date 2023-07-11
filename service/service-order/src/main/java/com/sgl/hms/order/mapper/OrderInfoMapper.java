package com.sgl.hms.order.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sgl.hms.model.order.OrderInfo;
import com.sgl.hms.vo.order.OrderCountQueryVo;
import com.sgl.hms.vo.order.OrderCountVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    //获取订单统计数据
    List<OrderCountVo> selectOrderCount(@Param("vo") OrderCountQueryVo orderCountQueryVo);
}
