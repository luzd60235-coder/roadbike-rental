package com.example.roadbikerental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 车辆配置实体。
 */
@Data
@TableName("bike_model")
public class BikeModel implements Serializable {

    @TableId(value = "model_id", type = IdType.AUTO)
    private Long modelId;

    private String brandName;

    private String seriesName;

    private String modelName;

    private String bikeType;

    private String frameMaterial;

    private String gearSystem;

    private String brakeType;

    private String wheelSize;

    private BigDecimal bikeWeight;

    private Integer suggestedHeightMin;

    private Integer suggestedHeightMax;

    private BigDecimal marketPrice;

    private String coverImageUrl;

    private String description;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
