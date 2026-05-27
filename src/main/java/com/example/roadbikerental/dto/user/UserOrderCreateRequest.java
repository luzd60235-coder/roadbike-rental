package com.example.roadbikerental.dto.user;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 用户创建订单请求。
 */
@Data
public class UserOrderCreateRequest {

    @NotNull(message = "车辆不能为空")
    private Long bikeId;

    @NotNull(message = "取车门店不能为空")
    private Long pickupStoreId;

    @NotNull(message = "计划取车时间不能为空")
    private LocalDateTime plannedStartTime;

    @NotNull(message = "计划还车时间不能为空")
    private LocalDateTime plannedEndTime;

    private String remark;
}
