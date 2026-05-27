package com.example.roadbikerental.dto.staff;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 员工租赁结算请求。
 */
@Data
public class StaffSettlementCreateRequest {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "基础租金不能为空")
    @DecimalMin(value = "0.00", message = "基础租金不能小于0")
    private BigDecimal baseRentAmount;

    @NotNull(message = "超时费用不能为空")
    @DecimalMin(value = "0.00", message = "超时费用不能小于0")
    private BigDecimal overtimeAmount;

    @NotNull(message = "维修赔偿不能为空")
    @DecimalMin(value = "0.00", message = "维修赔偿不能小于0")
    private BigDecimal maintenanceCompensation;

    @NotNull(message = "遗失赔偿不能为空")
    @DecimalMin(value = "0.00", message = "遗失赔偿不能小于0")
    private BigDecimal lossCompensation;

    @NotNull(message = "优惠金额不能为空")
    @DecimalMin(value = "0.00", message = "优惠金额不能小于0")
    private BigDecimal discountAmount;

    private String settlementNote;
}
