package com.example.roadbikerental.dto.user;

import lombok.Data;

/**
 * 用户个人资料更新请求。
 */
@Data
public class UserProfileUpdateRequest {

    private String nickname;

    private Integer gender;

    private String phone;

    private String email;

    private String idCardNo;

    private String password;

    private String confirmPassword;
}
