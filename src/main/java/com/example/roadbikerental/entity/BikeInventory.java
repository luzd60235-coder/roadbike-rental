package com.example.roadbikerental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 车辆库存实体。
 */
@Data
@TableName("bike_inventory")
public class BikeInventory implements Serializable {

    @TableId(value = "bike_id", type = IdType.AUTO)
    private Long bikeId;

    private String bikeCode;

    private Long modelId;

    private String frameNo;

    private String frameSize;

    private String color;

    private LocalDate purchaseDate;

    private BigDecimal purchasePrice;

    private BigDecimal mileageKm;

    private String gpsDeviceNo;

    private Long currentStoreId;

    private Integer bikeStatus;

    private LocalDateTime lastMaintenanceAt;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
