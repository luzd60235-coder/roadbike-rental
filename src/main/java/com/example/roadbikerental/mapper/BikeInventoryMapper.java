package com.example.roadbikerental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.roadbikerental.entity.BikeInventory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 车辆库存 Mapper。
 */
@Mapper
public interface BikeInventoryMapper extends BaseMapper<BikeInventory> {
}
