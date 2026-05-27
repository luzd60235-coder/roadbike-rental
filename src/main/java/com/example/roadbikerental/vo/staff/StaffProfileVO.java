package com.example.roadbikerental.vo.staff;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门店员工个人中心视图对象。
 */
@Data
public class StaffProfileVO {

    private Long staffId;

    private String username;

    private String staffName;

    private String phone;

    private String jobTitle;

    private String workSchedule;

    private Integer status;

    private Long storeId;

    private String storeName;

    private String storePhone;

    private String businessHours;

    private String storeAddress;

    private String currentShift;

    private String shiftPeriod;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    private Long pendingPickupCount;

    private Long rentingCount;

    private Long pendingSettlementCount;

    private Long maintenanceInProgressCount;

    private Long createdOrdersToday;

    private Long settlementsToday;

    private Long completedMaintenanceToday;
}
