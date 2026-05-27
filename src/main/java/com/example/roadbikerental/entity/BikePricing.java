package com.example.roadbikerental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格策略实体。
 */
@Data
@TableName("bike_pricing")
public class BikePricing implements Serializable {

    @TableId(value = "pricing_id", type = IdType.AUTO)
    private Long pricingId;

    private Long modelId;

    private BigDecimal dailyRent;

    private BigDecimal depositAmount;

    private BigDecimal overtimeFeePerHour;

    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTo;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
