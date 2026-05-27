package com.example.roadbikerental.controller.admin;

import com.example.roadbikerental.common.annotation.RoleRequired;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.result.ApiResponse;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.admin.AdminStoreStaffUpdateRequest;
import com.example.roadbikerental.service.StoreStaffService;
import com.example.roadbikerental.vo.admin.AdminStoreStaffVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员门店员工管理控制器。
 */
@RestController
@RequestMapping("/api/admin/store-staff")
@RoleRequired(RoleType.ADMIN)
public class AdminStoreStaffController {

    private final StoreStaffService storeStaffService;

    public AdminStoreStaffController(StoreStaffService storeStaffService) {
        this.storeStaffService = storeStaffService;
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<AdminStoreStaffVO>> page(@RequestParam(defaultValue = "1") Long current,
                                                           @RequestParam(defaultValue = "10") Long size,
                                                           @RequestParam(required = false) String keyword,
                                                           @RequestParam(required = false) Long storeId,
                                                           @RequestParam(required = false) Integer status) {
        return ApiResponse.success(storeStaffService.pageAdminStaffs(current, size, keyword, storeId, status));
    }

    @PutMapping("/{staffId}")
    public ApiResponse<AdminStoreStaffVO> update(@PathVariable Long staffId,
                                                 @Validated @RequestBody AdminStoreStaffUpdateRequest request) {
        return ApiResponse.success(storeStaffService.updateAdminStaff(staffId, request));
    }
}
