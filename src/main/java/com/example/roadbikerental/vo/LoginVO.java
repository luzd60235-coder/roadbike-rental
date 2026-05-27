package com.example.roadbikerental.vo;

import com.example.roadbikerental.common.enums.RoleType;
import lombok.Data;

/**
 * 登录响应对象。
 */
@Data
public class LoginVO {

    private Long userId;

    private String username;

    private String displayName;

    private RoleType roleType;

    private Long storeId;

    private String storeName;

    private String token;
}
