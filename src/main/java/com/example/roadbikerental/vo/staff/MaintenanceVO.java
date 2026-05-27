package com.example.roadbikerental.vo.staff;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 维修记录响应。
 */
@Data
public class MaintenanceVO {

    private Long maintenanceId;

    private Long bikeId;

    private String bikeCode;

    private Long storeId;

    private String storeName;

    private Long reportedByStaffId;

    private Long assignedStaffId;

    private String assignedStaffName;

    private String maintenanceType;

    private String faultDescription;

    private String maintenanceContent;

    private BigDecimal maintenanceCost;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer maintenanceStatus;

    private LocalDate nextMaintenanceDate;

    private String remark;

    private LocalDateTime createdAt;
}
