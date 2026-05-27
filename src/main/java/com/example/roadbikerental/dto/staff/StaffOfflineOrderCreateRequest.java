package com.example.roadbikerental.dto.staff;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 门店员工创建线下订单请求。
 */
@Data
public class StaffOfflineOrderCreateRequest {

    @NotNull(message = "用户不能为空")
    private Long userId;

    @NotNull(message = "车辆不能为空")
    private Long bikeId;

    @NotNull(message = "计划取车时间不能为空")
    private LocalDateTime plannedStartTime;

    @NotNull(message = "计划还车时间不能为空")
    private LocalDateTime plannedEndTime;

    private String remark;
}
