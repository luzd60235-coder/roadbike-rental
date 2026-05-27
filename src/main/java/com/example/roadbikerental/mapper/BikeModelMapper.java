package com.example.roadbikerental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.roadbikerental.entity.BikeModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 车辆配置 Mapper。
 */
@Mapper
public interface BikeModelMapper extends BaseMapper<BikeModel> {
}
