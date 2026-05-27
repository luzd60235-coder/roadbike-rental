package com.example.roadbikerental.dto.staff;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 门店员工注册请求。
 */
@Data
public class StaffRegisterRequest {

    @NotNull(message = "所属门店不能为空")
    private Long storeId;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @NotBlank(message = "员工姓名不能为空")
    private String staffName;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotNull(message = "性别不能为空")
    private Integer gender;

    @NotBlank(message = "身份证号不能为空")
    private String idCardNo;

    @NotBlank(message = "岗位不能为空")
    private String jobTitle;
}
