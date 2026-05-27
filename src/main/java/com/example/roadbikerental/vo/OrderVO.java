package com.example.roadbikerental.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单统一展示对象。
 */
@Data
public class OrderVO {

    private Long orderId;

    private String orderNo;

    private Long userId;

    private String username;

    private String nickname;

    private Long bikeId;

    private String bikeCode;

    private Long modelId;

    private String brandName;

    private String modelName;

    private Long pickupStoreId;

    private String pickupStoreName;

    private Long returnStoreId;

    private String returnStoreName;

    private Long createdStaffId;

    private String createdStaffName;

    private LocalDateTime plannedStartTime;

    private LocalDateTime plannedEndTime;

    private LocalDateTime actualPickupTime;

    private LocalDateTime actualReturnTime;

    private Integer rentDays;

    private BigDecimal dailyRentAmount;

    private BigDecimal rentAmount;

    private BigDecimal depositAmount;

    private BigDecimal overtimeFeePerHour;

    private BigDecimal overtimeAmount;

    private BigDecimal damageAmount;

    private BigDecimal otherAmount;

    private BigDecimal totalAmount;

    private Integer orderStatus;

    private Integer paymentStatus;

    private Integer depositRefundStatus;

    private String cancelReason;

    private String remark;

    private LocalDateTime createdAt;
}
