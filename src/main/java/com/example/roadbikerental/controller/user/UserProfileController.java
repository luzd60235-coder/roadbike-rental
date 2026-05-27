package com.example.roadbikerental.controller.user;

import com.example.roadbikerental.common.annotation.RoleRequired;
import com.example.roadbikerental.common.auth.AuthContext;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.result.ApiResponse;
import com.example.roadbikerental.dto.user.UserProfileUpdateRequest;
import com.example.roadbikerental.entity.AppUser;
import com.example.roadbikerental.service.AppUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户个人中心控制器。
 */
@RestController
@RequestMapping("/api/user/profile")
@RoleRequired(RoleType.USER)
public class UserProfileController {

    private final AppUserService appUserService;

    public UserProfileController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    /**
     * 获取当前登录用户资料。
     */
    @GetMapping("/me")
    public ApiResponse<AppUser> me() {
        AppUser appUser = appUserService.getCurrentUser(AuthContext.getUserId());
        appUser.setPasswordHash(null);
        return ApiResponse.success(appUser);
    }

    /**
     * 更新当前用户资料。
     */
    @PutMapping("/me")
    public ApiResponse<AppUser> update(@Validated @RequestBody UserProfileUpdateRequest request) {
        AppUser appUser = appUserService.updateProfile(AuthContext.getUserId(), request);
        appUser.setPasswordHash(null);
        return ApiResponse.success(appUser);
    }
}
