package com.example.roadbikerental.controller.admin;

import com.example.roadbikerental.common.annotation.RoleRequired;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.result.ApiResponse;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.admin.AdminStoreSaveRequest;
import com.example.roadbikerental.entity.RentalStore;
import com.example.roadbikerental.service.RentalStoreService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台门店管理控制器。
 */
@RestController
@RequestMapping("/api/admin/stores")
@RoleRequired(RoleType.ADMIN)
public class AdminStoreController {

    private final RentalStoreService rentalStoreService;

    public AdminStoreController(RentalStoreService rentalStoreService) {
        this.rentalStoreService = rentalStoreService;
    }

    /**
     * 门店分页查询。
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<RentalStore>> page(@RequestParam(defaultValue = "1") Long current,
                                                     @RequestParam(defaultValue = "10") Long size,
                                                     @RequestParam(required = false) String city,
                                                     @RequestParam(required = false) Integer status) {
        return ApiResponse.success(rentalStoreService.pageStores(current, size, city, status));
    }

    /**
     * 门店详情。
     */
    @GetMapping("/{storeId}")
    public ApiResponse<RentalStore> detail(@PathVariable Long storeId) {
        return ApiResponse.success(rentalStoreService.getById(storeId));
    }

    /**
     * 新增门店。
     */
    @PostMapping
    public ApiResponse<RentalStore> save(@Validated @RequestBody AdminStoreSaveRequest request) {
        return ApiResponse.success(rentalStoreService.saveOrUpdateStore(request));
    }

    /**
     * 修改门店。
     */
    @PutMapping("/{storeId}")
    public ApiResponse<RentalStore> update(@PathVariable Long storeId,
                                           @Validated @RequestBody AdminStoreSaveRequest request) {
        request.setStoreId(storeId);
        return ApiResponse.success(rentalStoreService.saveOrUpdateStore(request));
    }

    /**
     * 删除门店。
     */
    @DeleteMapping("/{storeId}")
    public ApiResponse<Void> delete(@PathVariable Long storeId) {
        rentalStoreService.removeById(storeId);
        return ApiResponse.success("删除成功", null);
    }
}
