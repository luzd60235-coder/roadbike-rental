package com.example.roadbikerental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.roadbikerental.entity.BikePricing;
import org.apache.ibatis.annotations.Mapper;

/**
 * 价格策略 Mapper。
 */
@Mapper
public interface BikePricingMapper extends BaseMapper<BikePricing> {
}
