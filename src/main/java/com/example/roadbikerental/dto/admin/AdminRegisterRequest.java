package com.example.roadbikerental.dto.admin;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 管理员注册请求。
 */
@Data
public class AdminRegisterRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "身份证号不能为空")
    private String idCardNo;

    private String email;
}
