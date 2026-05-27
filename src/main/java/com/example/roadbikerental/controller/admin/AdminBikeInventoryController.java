package com.example.roadbikerental.controller.admin;

import com.example.roadbikerental.common.annotation.RoleRequired;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.result.ApiResponse;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.admin.AdminBikeInventorySaveRequest;
import com.example.roadbikerental.entity.BikeInventory;
import com.example.roadbikerental.service.BikeInventoryService;
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
 * 后台车辆库存管理控制器。
 */
@RestController
@RequestMapping("/api/admin/bikes")
@RoleRequired(RoleType.ADMIN)
public class AdminBikeInventoryController {

    private final BikeInventoryService bikeInventoryService;

    public AdminBikeInventoryController(BikeInventoryService bikeInventoryService) {
        this.bikeInventoryService = bikeInventoryService;
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<BikeInventory>> page(@RequestParam(defaultValue = "1") Long current,
                                                       @RequestParam(defaultValue = "10") Long size,
                                                       @RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) Long storeId,
                                                       @RequestParam(required = false) Long modelId,
                                                       @RequestParam(required = false) Integer status) {
        return ApiResponse.success(
                bikeInventoryService.pageAdminInventory(current, size, keyword, storeId, modelId, status)
        );
    }

    @GetMapping("/{bikeId}")
    public ApiResponse<BikeInventory> detail(@PathVariable Long bikeId) {
        return ApiResponse.success(bikeInventoryService.getRequiredById(bikeId));
    }

    @PostMapping
    public ApiResponse<BikeInventory> save(@Validated @RequestBody AdminBikeInventorySaveRequest request) {
        return ApiResponse.success(bikeInventoryService.saveOrUpdateInventory(request));
    }

    @PutMapping("/{bikeId}")
    public ApiResponse<BikeInventory> update(@PathVariable Long bikeId,
                                             @Validated @RequestBody AdminBikeInventorySaveRequest request) {
        request.setBikeId(bikeId);
        return ApiResponse.success(bikeInventoryService.saveOrUpdateInventory(request));
    }

    @DeleteMapping("/{bikeId}")
    public ApiResponse<Void> delete(@PathVariable Long bikeId) {
        bikeInventoryService.removeById(bikeId);
        return ApiResponse.success("删除成功", null);
    }
}
