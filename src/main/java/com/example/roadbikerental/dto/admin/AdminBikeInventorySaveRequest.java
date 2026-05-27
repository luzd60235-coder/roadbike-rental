package com.example.roadbikerental.dto.admin;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 后台车辆库存保存请求。
 */
@Data
public class AdminBikeInventorySaveRequest {

    private Long bikeId;

    @NotBlank(message = "车辆编码不能为空")
    private String bikeCode;

    @NotNull(message = "车辆配置不能为空")
    private Long modelId;

    @NotBlank(message = "车架编号不能为空")
    private String frameNo;

    @NotBlank(message = "车辆尺码不能为空")
    private String frameSize;

    private String color;

    private LocalDate purchaseDate;

    private BigDecimal purchasePrice;

    private BigDecimal mileageKm;

    private String gpsDeviceNo;

    @NotNull(message = "所在门店不能为空")
    private Long currentStoreId;

    @NotNull(message = "车辆状态不能为空")
    private Integer bikeStatus;

    private LocalDateTime lastMaintenanceAt;

    private String remark;
}
