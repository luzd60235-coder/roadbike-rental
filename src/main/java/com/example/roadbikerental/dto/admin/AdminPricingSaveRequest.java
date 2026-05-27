package com.example.roadbikerental.dto.admin;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台价格策略保存请求。
 */
@Data
public class AdminPricingSaveRequest {

    private Long pricingId;

    @NotNull(message = "车辆配置不能为空")
    private Long modelId;

    @NotNull(message = "日租金不能为空")
    @DecimalMin(value = "0.00", message = "日租金不能小于0")
    private BigDecimal dailyRent;

    @NotNull(message = "押金不能为空")
    @DecimalMin(value = "0.00", message = "押金不能小于0")
    private BigDecimal depositAmount;

    @NotNull(message = "超时费不能为空")
    @DecimalMin(value = "0.00", message = "超时费不能小于0")
    private BigDecimal overtimeFeePerHour;

    @NotNull(message = "生效时间不能为空")
    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTo;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
