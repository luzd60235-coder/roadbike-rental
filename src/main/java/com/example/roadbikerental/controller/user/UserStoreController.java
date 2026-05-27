package com.example.roadbikerental.controller.user;

import com.example.roadbikerental.common.annotation.RoleRequired;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.result.ApiResponse;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.entity.RentalStore;
import com.example.roadbikerental.service.RentalStoreService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户门店查询控制器。
 */
@RestController
@RequestMapping("/api/user/stores")
@RoleRequired(RoleType.USER)
public class UserStoreController {

    private final RentalStoreService rentalStoreService;

    public UserStoreController(RentalStoreService rentalStoreService) {
        this.rentalStoreService = rentalStoreService;
    }

    /**
     * 用户分页查询门店。
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<RentalStore>> page(@RequestParam(defaultValue = "1") Long current,
                                                     @RequestParam(defaultValue = "10") Long size,
                                                     @RequestParam(required = false) String city) {
        return ApiResponse.success(rentalStoreService.pageStores(current, size, city, 1));
    }

    /**
     * 用户查看门店详情。
     */
    @GetMapping("/{storeId}")
    public ApiResponse<RentalStore> detail(@PathVariable Long storeId) {
        return ApiResponse.success(rentalStoreService.getById(storeId));
    }
}
