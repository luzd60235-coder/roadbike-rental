package com.example.roadbikerental.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 财务统计视图实体。
 */
@Data
@TableName("vw_financial_overview")
public class FinancialOverview implements Serializable {

    private Long totalOrders;

    private BigDecimal totalRentIncome;

    private BigDecimal totalDepositCollected;

    private BigDecimal totalDepositRefunded;

    private BigDecimal totalExtraIncome;

    private BigDecimal totalMaintenanceCost;

    private BigDecimal estimatedNetIncome;
}
