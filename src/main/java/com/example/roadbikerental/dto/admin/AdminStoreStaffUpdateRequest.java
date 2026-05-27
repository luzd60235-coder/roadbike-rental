package com.example.roadbikerental.dto.admin;

import lombok.Data;

/**
 * 管理员更新门店员工请求。
 */
@Data
public class AdminStoreStaffUpdateRequest {

    private String staffName;

    private String phone;

    private String jobTitle;

    private String workSchedule;

    private Integer status;

    private String password;

    private String confirmPassword;
}
