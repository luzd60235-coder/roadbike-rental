package com.example.roadbikerental.vo.staff;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 门店结算响应。
 */
@Data
public class SettlementVO {

    private Long settlementId;

    private Long orderId;

    private String orderNo;

    private Long settleStoreId;

    private String settleStoreName;

    private Long settleStaffId;

    private String settleStaffName;

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
}
