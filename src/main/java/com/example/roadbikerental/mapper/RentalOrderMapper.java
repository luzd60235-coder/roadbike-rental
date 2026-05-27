package com.example.roadbikerental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.roadbikerental.entity.RentalOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单 Mapper。
 */
@Mapper
public interface RentalOrderMapper extends BaseMapper<RentalOrder> {
}
