package com.example.roadbikerental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单结算实体。
 */
@Data
@TableName("rental_order_settlement")
public class RentalOrderSettlement implements Serializable {

    @TableId(value = "settlement_id", type = IdType.AUTO)
    private Long settlementId;

    private Long orderId;

    private Long settleStoreId;

    private Long settleStaffId;

    private BigDecimal baseRentAmount;

    private BigDecimal overtimeAmount;

    private BigDecimal maintenanceCompensation;

    private BigDecimal lossCompensation;

    private BigDecimal discountAmount;

    private BigDecimal payableTotal;

    private BigDecimal depositPaid;

    private BigDecimal refundDepositAmount;

    private String settlementNote;

    private LocalDateTime settledAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
