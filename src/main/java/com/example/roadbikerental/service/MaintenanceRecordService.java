package com.example.roadbikerental.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.staff.StaffMaintenanceSaveRequest;
import com.example.roadbikerental.dto.staff.StaffMaintenanceUpdateRequest;
import com.example.roadbikerental.entity.MaintenanceRecord;
import com.example.roadbikerental.vo.staff.MaintenanceVO;

/**
 * 维修记录服务。
 */
public interface MaintenanceRecordService extends IService<MaintenanceRecord> {

    MaintenanceVO createRecord(Long staffId, Long storeId, StaffMaintenanceSaveRequest request);

    MaintenanceVO updateRecord(Long maintenanceId, Long storeId, StaffMaintenanceUpdateRequest request);

    PageResult<MaintenanceVO> pageRecords(Long current, Long size, Long storeId, Integer status);
}
