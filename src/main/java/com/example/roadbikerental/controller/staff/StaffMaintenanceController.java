package com.example.roadbikerental.controller.staff;

import com.example.roadbikerental.common.annotation.RoleRequired;
import com.example.roadbikerental.common.auth.AuthContext;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.result.ApiResponse;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.staff.StaffMaintenanceSaveRequest;
import com.example.roadbikerental.dto.staff.StaffMaintenanceUpdateRequest;
import com.example.roadbikerental.entity.BikeInventory;
import com.example.roadbikerental.entity.BikeModel;
import com.example.roadbikerental.entity.StoreStaff;
import com.example.roadbikerental.service.BikeInventoryService;
import com.example.roadbikerental.service.BikeModelService;
import com.example.roadbikerental.service.MaintenanceRecordService;
import com.example.roadbikerental.service.StoreStaffService;
import com.example.roadbikerental.vo.staff.MaintenanceVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
 * 门店维修管理控制器。
 */
@RestController
@RequestMapping("/api/staff/maintenance")
@RoleRequired(RoleType.STAFF)
public class StaffMaintenanceController {

    private final MaintenanceRecordService maintenanceRecordService;
    private final BikeInventoryService bikeInventoryService;
    private final BikeModelService bikeModelService;
    private final StoreStaffService storeStaffService;

    public StaffMaintenanceController(MaintenanceRecordService maintenanceRecordService,
                                      BikeInventoryService bikeInventoryService,
                                      BikeModelService bikeModelService,
                                      StoreStaffService storeStaffService) {
        this.maintenanceRecordService = maintenanceRecordService;
        this.bikeInventoryService = bikeInventoryService;
        this.bikeModelService = bikeModelService;
        this.storeStaffService = storeStaffService;
    }

    /**
     * 门店创建维修记录。
     */
    @PostMapping
    public ApiResponse<MaintenanceVO> create(@Validated @RequestBody StaffMaintenanceSaveRequest request) {
        return ApiResponse.success(maintenanceRecordService.createRecord(
                AuthContext.getUserId(), AuthContext.getStoreId(), request));
    }

    /**
     * 门店维修工单表单选项。
     */
    @GetMapping("/form-options")
    public ApiResponse<Map<String, Object>> formOptions() {
        List<BikeInventory> bikes = bikeInventoryService.list(new LambdaQueryWrapper<BikeInventory>()
                .eq(BikeInventory::getCurrentStoreId, AuthContext.getStoreId())
                .orderByDesc(BikeInventory::getCreatedAt));
        Set<Long> modelIds = bikes.stream().map(BikeInventory::getModelId).collect(Collectors.toSet());
        Map<Long, BikeModel> modelMap = modelIds.isEmpty()
                ? Collections.emptyMap()
                : bikeModelService.listByIds(modelIds).stream()
                .collect(Collectors.toMap(BikeModel::getModelId, Function.identity()));

        List<StoreStaff> staffs = storeStaffService.list(new LambdaQueryWrapper<StoreStaff>()
                .eq(StoreStaff::getStoreId, AuthContext.getStoreId())
                .eq(StoreStaff::getStatus, 1)
                .orderByAsc(StoreStaff::getStaffId));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("bikes", bikes.stream().map(item -> {
            BikeModel model = modelMap.get(item.getModelId());
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("value", item.getBikeId());
            value.put("label", item.getBikeCode());
            value.put("bikeCode", item.getBikeCode());
            value.put("bikeName", buildBikeName(model, item.getBikeCode()));
            value.put("bikeStatus", item.getBikeStatus());
            return value;
        }).collect(Collectors.toList()));
        result.put("staffs", staffs.stream().map(item -> {
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("value", item.getStaffId());
            value.put("label", item.getStaffName());
            value.put("jobTitle", item.getJobTitle());
            return value;
        }).collect(Collectors.toList()));
        return ApiResponse.success(result);
    }

    /**
     * 门店更新维修记录。
     */
    @PutMapping("/{maintenanceId}")
    public ApiResponse<MaintenanceVO> update(@PathVariable Long maintenanceId,
                                             @RequestBody StaffMaintenanceUpdateRequest request) {
        return ApiResponse.success(maintenanceRecordService.updateRecord(
                maintenanceId, AuthContext.getStoreId(), request));
    }

    /**
     * 门店分页查询维修记录。
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<MaintenanceVO>> page(@RequestParam(defaultValue = "1") Long current,
                                                       @RequestParam(defaultValue = "10") Long size,
                                                       @RequestParam(required = false) Integer status) {
        return ApiResponse.success(maintenanceRecordService.pageRecords(current, size, AuthContext.getStoreId(), status));
    }

    private String buildBikeName(BikeModel model, String fallback) {
        if (model == null) {
            return fallback;
        }
        String name = StringUtils.trimToEmpty(model.getBrandName()) + " " + StringUtils.trimToEmpty(model.getModelName());
        return StringUtils.defaultIfBlank(name.trim(), fallback);
    }
}
