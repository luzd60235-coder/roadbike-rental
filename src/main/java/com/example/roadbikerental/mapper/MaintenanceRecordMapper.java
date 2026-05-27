package com.example.roadbikerental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.roadbikerental.entity.MaintenanceRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 维修记录 Mapper。
 */
@Mapper
public interface MaintenanceRecordMapper extends BaseMapper<MaintenanceRecord> {
}
