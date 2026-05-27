package com.example.roadbikerental.controller.staff;

import com.example.roadbikerental.common.annotation.RoleRequired;
import com.example.roadbikerental.common.auth.AuthContext;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.result.ApiResponse;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.staff.StaffOfflineOrderCreateRequest;
import com.example.roadbikerental.entity.AppUser;
import com.example.roadbikerental.entity.BikeInventory;
import com.example.roadbikerental.entity.BikeModel;
import com.example.roadbikerental.dto.staff.StaffPickupRequest;
import com.example.roadbikerental.dto.staff.StaffReturnRequest;
import com.example.roadbikerental.service.AppUserService;
import com.example.roadbikerental.service.BikeInventoryService;
import com.example.roadbikerental.service.BikeModelService;
import com.example.roadbikerental.service.RentalOrderService;
import org.apache.commons.lang3.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.roadbikerental.vo.OrderVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 门店车辆交易控制器。
 */
@RestController
@RequestMapping("/api/staff/operations")
@RoleRequired(RoleType.STAFF)
public class StaffOperationController {

    private final RentalOrderService rentalOrderService;
    private final AppUserService appUserService;
    private final BikeInventoryService bikeInventoryService;
    private final BikeModelService bikeModelService;

    public StaffOperationController(RentalOrderService rentalOrderService,
                                    AppUserService appUserService,
                                    BikeInventoryService bikeInventoryService,
                                    BikeModelService bikeModelService) {
        this.rentalOrderService = rentalOrderService;
        this.appUserService = appUserService;
        this.bikeInventoryService = bikeInventoryService;
        this.bikeModelService = bikeModelService;
    }

    /**
     * 门店订单分页查询。
     */
    @GetMapping("/orders/page")
    public ApiResponse<PageResult<OrderVO>> pageOrders(@RequestParam(defaultValue = "1") Long current,
                                                       @RequestParam(defaultValue = "10") Long size,
                                                       @RequestParam(required = false) Integer status,
                                                       @RequestParam(required = false) String keyword) {
        return ApiResponse.success(rentalOrderService.pageStoreOrders(
                current, size, AuthContext.getStoreId(), status, keyword));
    }

    /**
     * 门店新建线下订单。
     */
    @PostMapping("/orders")
    public ApiResponse<OrderVO> createOfflineOrder(@Validated @RequestBody StaffOfflineOrderCreateRequest request) {
        return ApiResponse.success(rentalOrderService.createStoreOrder(
                AuthContext.getUserId(), AuthContext.getStoreId(), request));
    }

    /**
     * 门店线下订单表单选项。
     */
    @GetMapping("/orders/form-options")
    public ApiResponse<Map<String, Object>> orderFormOptions() {
        List<AppUser> users = appUserService.list(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getStatus, 1)
                .orderByDesc(AppUser::getCreatedAt));

        List<BikeInventory> bikes = bikeInventoryService.list(new LambdaQueryWrapper<BikeInventory>()
                .eq(BikeInventory::getCurrentStoreId, AuthContext.getStoreId())
                .eq(BikeInventory::getBikeStatus, 1)
                .orderByDesc(BikeInventory::getCreatedAt));

        Set<Long> modelIds = bikes.stream().map(BikeInventory::getModelId).collect(Collectors.toSet());
        Map<Long, BikeModel> modelMap = modelIds.isEmpty()
                ? Collections.emptyMap()
                : bikeModelService.listByIds(modelIds).stream()
                .collect(Collectors.toMap(BikeModel::getModelId, Function.identity()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("users", users.stream().map(item -> {
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("value", item.getUserId());
            value.put("label", StringUtils.defaultIfBlank(item.getNickname(), item.getUsername()));
            value.put("username", item.getUsername());
            value.put("phone", item.getPhone());
            return value;
        }).collect(Collectors.toList()));
        result.put("bikes", bikes.stream().map(item -> {
            BikeModel model = modelMap.get(item.getModelId());
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("value", item.getBikeId());
            value.put("label", item.getBikeCode());
            value.put("bikeCode", item.getBikeCode());
            value.put("bikeName", buildBikeName(model, item.getBikeCode()));
            value.put("frameSize", item.getFrameSize());
            value.put("color", item.getColor());
            return value;
        }).collect(Collectors.toList()));
        return ApiResponse.success(result);
    }

    /**
     * 员工办理取车。
     */
    @PostMapping("/pickup")
    public ApiResponse<OrderVO> pickup(@Validated @RequestBody StaffPickupRequest request) {
        return ApiResponse.success(rentalOrderService.pickupBike(AuthContext.getUserId(), AuthContext.getStoreId(), request));
    }

    /**
     * 员工办理还车。
     */
    @PostMapping("/return")
    public ApiResponse<OrderVO> returnBike(@Validated @RequestBody StaffReturnRequest request) {
        return ApiResponse.success(rentalOrderService.returnBike(AuthContext.getUserId(), AuthContext.getStoreId(), request));
    }

    private String buildBikeName(BikeModel model, String fallback) {
        if (model == null) {
            return fallback;
        }
        String name = StringUtils.trimToEmpty(model.getBrandName()) + " " + StringUtils.trimToEmpty(model.getModelName());
        return StringUtils.defaultIfBlank(name.trim(), fallback);
    }
}
