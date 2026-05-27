package com.example.roadbikerental.dto.staff;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 员工办理还车请求。
 */
@Data
public class StaffReturnRequest {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;
}
