package com.example.roadbikerental.controller.admin;

import com.example.roadbikerental.common.annotation.RoleRequired;
import com.example.roadbikerental.common.auth.AuthContext;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.result.ApiResponse;
import com.example.roadbikerental.dto.admin.AdminProfileUpdateRequest;
import com.example.roadbikerental.entity.SysAdmin;
import com.example.roadbikerental.service.SysAdminService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员个人中心控制器。
 */
@RestController
@RequestMapping("/api/admin/profile")
@RoleRequired(RoleType.ADMIN)
public class AdminProfileController {

    private final SysAdminService sysAdminService;

    public AdminProfileController(SysAdminService sysAdminService) {
        this.sysAdminService = sysAdminService;
    }

    @GetMapping("/me")
    public ApiResponse<SysAdmin> me() {
        SysAdmin admin = sysAdminService.getCurrentAdmin(AuthContext.getUserId());
        admin.setPasswordHash(null);
        return ApiResponse.success(admin);
    }

    @PutMapping("/me")
    public ApiResponse<SysAdmin> update(@Validated @RequestBody AdminProfileUpdateRequest request) {
        SysAdmin admin = sysAdminService.updateProfile(AuthContext.getUserId(), request);
        admin.setPasswordHash(null);
        return ApiResponse.success(admin);
    }
}
