package com.example.roadbikerental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 车辆维修记录实体。
 */
@Data
@TableName("maintenance_record")
public class MaintenanceRecord implements Serializable {

    @TableId(value = "maintenance_id", type = IdType.AUTO)
    private Long maintenanceId;

    private Long bikeId;

    private Long storeId;

    private Long reportedByStaffId;

    private Long assignedStaffId;

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

    private LocalDateTime updatedAt;
}
