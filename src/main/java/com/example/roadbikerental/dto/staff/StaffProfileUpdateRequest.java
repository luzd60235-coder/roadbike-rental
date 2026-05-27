package com.example.roadbikerental.dto.staff;

import lombok.Data;

/**
 * 门店员工个人资料更新请求。
 */
@Data
public class StaffProfileUpdateRequest {

    private String staffName;

    private String phone;

    private String password;

    private String confirmPassword;
}
