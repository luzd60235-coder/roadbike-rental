package com.example.roadbikerental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 租赁订单实体。
 */
@Data
@TableName("rental_order")
public class RentalOrder implements Serializable {

    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    private String orderNo;

    private Long userId;

    private Long bikeId;

    private Long pricingId;

    private Long pickupStoreId;

    private Long returnStoreId;

    private Long createdStaffId;

    private LocalDateTime plannedStartTime;

    private LocalDateTime plannedEndTime;

    private LocalDateTime actualPickupTime;

    private LocalDateTime actualReturnTime;

    private Integer rentDays;

    private BigDecimal dailyRentAmount;

    private BigDecimal rentAmount;

    private BigDecimal depositAmount;

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

    private LocalDateTime updatedAt;
}
