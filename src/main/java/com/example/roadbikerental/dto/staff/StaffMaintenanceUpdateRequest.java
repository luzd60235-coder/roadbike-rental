package com.example.roadbikerental.dto.staff;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工维修更新请求。
 */
@Data
public class StaffMaintenanceUpdateRequest {

    private Long assignedStaffId;

    private String maintenanceContent;

    private BigDecimal maintenanceCost;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer maintenanceStatus;

    private LocalDate nextMaintenanceDate;

    private String remark;
}
