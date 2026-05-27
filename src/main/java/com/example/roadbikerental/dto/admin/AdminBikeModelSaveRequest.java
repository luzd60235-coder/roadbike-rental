package com.example.roadbikerental.dto.admin;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 后台车辆配置保存请求。
 */
@Data
public class AdminBikeModelSaveRequest {

    private Long modelId;

    @NotBlank(message = "品牌不能为空")
    private String brandName;

    private String seriesName;

    @NotBlank(message = "车型名称不能为空")
    private String modelName;

    @NotBlank(message = "车辆类型不能为空")
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

    @NotNull(message = "状态不能为空")
    private Integer status;
}
