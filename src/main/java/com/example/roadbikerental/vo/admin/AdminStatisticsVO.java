package com.example.roadbikerental.vo.admin;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 后台统计概览响应。
 */
@Data
public class AdminStatisticsVO {

    private Long totalUsers;

    private Long totalStores;

    private Long totalBikeModels;

    private Long totalBikes;

    private Long totalOrders;

    private BigDecimal totalRentIncome;

    private BigDecimal totalDepositCollected;

    private BigDecimal totalDepositRefunded;

    private BigDecimal totalExtraIncome;

    private BigDecimal totalMaintenanceCost;

    private BigDecimal estimatedNetIncome;
}
