package com.example.roadbikerental.dto.admin;

import lombok.Data;

/**
 * 管理员个人资料更新请求。
 */
@Data
public class AdminProfileUpdateRequest {

    private String realName;

    private String phone;

    private String email;

    private String idCardNo;

    private String password;

    private String confirmPassword;
}
