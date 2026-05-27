package com.example.roadbikerental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.roadbikerental.entity.RentalOrderSettlement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单结算 Mapper。
 */
@Mapper
public interface RentalOrderSettlementMapper extends BaseMapper<RentalOrderSettlement> {
}
