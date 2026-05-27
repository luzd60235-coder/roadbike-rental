package com.example.roadbikerental.controller.staff;

import com.example.roadbikerental.common.annotation.RoleRequired;
import com.example.roadbikerental.common.auth.AuthContext;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.result.ApiResponse;
import com.example.roadbikerental.dto.staff.StaffProfileUpdateRequest;
import com.example.roadbikerental.service.StoreStaffService;
import com.example.roadbikerental.vo.staff.StaffProfileVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门店员工个人中心控制器。
 */
@RestController
@RequestMapping("/api/staff/profile")
@RoleRequired(RoleType.STAFF)
public class StaffProfileController {

    private final StoreStaffService storeStaffService;

    public StaffProfileController(StoreStaffService storeStaffService) {
        this.storeStaffService = storeStaffService;
    }

    /**
     * 获取当前登录员工资料和当班数据。
     */
    @GetMapping("/me")
    public ApiResponse<StaffProfileVO> me() {
        return ApiResponse.success(storeStaffService.getProfile(AuthContext.getUserId()));
    }

    /**
     * 更新当前登录员工资料。
     */
    @PutMapping("/me")
    public ApiResponse<StaffProfileVO> update(@Validated @RequestBody StaffProfileUpdateRequest request) {
        return ApiResponse.success(storeStaffService.updateProfile(AuthContext.getUserId(), request));
    }
}
