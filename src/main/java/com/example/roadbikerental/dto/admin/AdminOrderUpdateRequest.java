package com.example.roadbikerental.dto.admin;

import lombok.Data;

/**
 * 后台订单管理更新请求。
 */
@Data
public class AdminOrderUpdateRequest {

    private Integer orderStatus;

    private String cancelReason;

    private String remark;
}
