package com.example.roadbikerental.controller.staff;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.roadbikerental.common.result.ApiResponse;
import com.example.roadbikerental.dto.LoginRequest;
import com.example.roadbikerental.dto.staff.StaffRegisterRequest;
import com.example.roadbikerental.entity.RentalStore;
import com.example.roadbikerental.service.RentalStoreService;
import com.example.roadbikerental.service.StoreStaffService;
import com.example.roadbikerental.vo.LoginVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 门店员工登录控制器。
 */
@RestController
@RequestMapping("/api/staff/auth")
public class StaffAuthController {

    private final StoreStaffService storeStaffService;
    private final RentalStoreService rentalStoreService;

    public StaffAuthController(StoreStaffService storeStaffService, RentalStoreService rentalStoreService) {
        this.storeStaffService = storeStaffService;
        this.rentalStoreService = rentalStoreService;
    }

    /**
     * 员工注册所需门店选项。
     */
    @GetMapping("/register/options")
    public ApiResponse<List<RentalStore>> registerOptions() {
        return ApiResponse.success(rentalStoreService.list(new LambdaQueryWrapper<RentalStore>()
                .select(RentalStore::getStoreId, RentalStore::getStoreName, RentalStore::getCity, RentalStore::getDistrict, RentalStore::getStatus)
                .eq(RentalStore::getStatus, 1)
                .orderByAsc(RentalStore::getCity)
                .orderByAsc(RentalStore::getStoreId)));
    }

    /**
     * 门店员工注册。
     */
    @PostMapping("/register")
    public ApiResponse<LoginVO> register(@Validated @RequestBody StaffRegisterRequest request) {
        return ApiResponse.success(storeStaffService.register(request));
    }

    /**
     * 门店员工登录。
     */
    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Validated @RequestBody LoginRequest request) {
        return ApiResponse.success(storeStaffService.login(request));
    }
}
