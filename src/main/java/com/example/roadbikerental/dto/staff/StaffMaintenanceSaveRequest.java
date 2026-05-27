package com.example.roadbikerental.dto.staff;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工维修建单请求。
 */
@Data
public class StaffMaintenanceSaveRequest {

    @NotNull(message = "车辆ID不能为空")
    private Long bikeId;

    private Long assignedStaffId;

    @NotBlank(message = "维修类型不能为空")
    private String maintenanceType;

    @NotBlank(message = "故障描述不能为空")
    private String faultDescription;

    private String maintenanceContent;

    private BigDecimal maintenanceCost;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer maintenanceStatus;

    private LocalDate nextMaintenanceDate;

    private String remark;
}
