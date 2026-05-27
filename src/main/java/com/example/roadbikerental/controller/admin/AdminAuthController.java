package com.example.roadbikerental.controller.admin;

import com.example.roadbikerental.common.result.ApiResponse;
import com.example.roadbikerental.dto.LoginRequest;
import com.example.roadbikerental.dto.admin.AdminRegisterRequest;
import com.example.roadbikerental.service.SysAdminService;
import com.example.roadbikerental.vo.LoginVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台登录控制器。
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final SysAdminService sysAdminService;

    public AdminAuthController(SysAdminService sysAdminService) {
        this.sysAdminService = sysAdminService;
    }

    /**
     * 管理员注册。
     */
    @PostMapping("/register")
    public ApiResponse<LoginVO> register(@Validated @RequestBody AdminRegisterRequest request) {
        return ApiResponse.success(sysAdminService.register(request));
    }

    /**
     * 管理员登录。
     */
    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Validated @RequestBody LoginRequest request) {
        return ApiResponse.success(sysAdminService.login(request));
    }
}
