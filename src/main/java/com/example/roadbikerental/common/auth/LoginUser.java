package com.example.roadbikerental.common.auth;

import com.example.roadbikerental.common.enums.RoleType;
import lombok.Data;

/**
 * 当前登录用户上下文对象。
 */
@Data
public class LoginUser {

    private Long userId;

    private String username;

    private String displayName;

    private RoleType roleType;

    private Long storeId;
}
