package com.example.roadbikerental.vo.user;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户端车辆查询响应。
 */
@Data
public class UserBikeVO {

    private Long bikeId;

    private String bikeCode;

    private Long modelId;

    private String brandName;

    private String modelName;

    private String bikeType;

    private String frameMaterial;

    private String gearSystem;

    private String brakeType;

    private String wheelSize;

    private BigDecimal bikeWeight;

    private String frameSize;

    private String color;

    private Long storeId;

    private String storeName;

    private Integer bikeStatus;

    private BigDecimal dailyRent;

    private BigDecimal originalDailyRent;

    private BigDecimal depositAmount;

    private Integer maintenanceHistoryCount;

    private BigDecimal maintenanceDiscountRate;

    private String coverImageUrl;

    private String description;

    private BigDecimal mileageKm;
}
