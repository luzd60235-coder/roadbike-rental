package com.example.roadbikerental.vo.user;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserBikeMaintenanceVO {

    private Long maintenanceId;

    private Long bikeId;

    private String bikeCode;

    private Long storeId;

    private String storeName;

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
