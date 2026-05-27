package com.example.roadbikerental.controller.user;

import com.example.roadbikerental.common.result.ApiResponse;
import com.example.roadbikerental.dto.LoginRequest;
import com.example.roadbikerental.dto.user.UserRegisterRequest;
import com.example.roadbikerental.service.AppUserService;
import com.example.roadbikerental.vo.LoginVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户认证控制器。
 */
@RestController
@RequestMapping("/api/user/auth")
public class UserAuthController {

    private final AppUserService appUserService;

    public UserAuthController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    /**
     * 用户注册。
     */
    @PostMapping("/register")
    public ApiResponse<LoginVO> register(@Validated @RequestBody UserRegisterRequest request) {
        return ApiResponse.success(appUserService.register(request));
    }

    /**
     * 用户登录。
     */
    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Validated @RequestBody LoginRequest request) {
        return ApiResponse.success(appUserService.login(request));
    }
}
