package com.example.roadbikerental.controller.admin;

import com.example.roadbikerental.common.annotation.RoleRequired;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.result.ApiResponse;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.admin.AdminBikeModelSaveRequest;
import com.example.roadbikerental.entity.BikeModel;
import com.example.roadbikerental.service.BikeModelService;
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
 * 后台车辆配置管理控制器。
 */
@RestController
@RequestMapping("/api/admin/bike-models")
@RoleRequired(RoleType.ADMIN)
public class AdminBikeModelController {

    private final BikeModelService bikeModelService;

    public AdminBikeModelController(BikeModelService bikeModelService) {
        this.bikeModelService = bikeModelService;
    }

    /**
     * 车辆配置分页查询。
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<BikeModel>> page(@RequestParam(defaultValue = "1") Long current,
                                                   @RequestParam(defaultValue = "10") Long size,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) Integer status) {
        return ApiResponse.success(bikeModelService.pageModels(current, size, keyword, status));
    }

    /**
     * 根据主键查询车辆配置。
     */
    @GetMapping("/{modelId}")
    public ApiResponse<BikeModel> detail(@PathVariable Long modelId) {
        return ApiResponse.success(bikeModelService.getById(modelId));
    }

    /**
     * 新增车辆配置。
     */
    @PostMapping
    public ApiResponse<BikeModel> save(@Validated @RequestBody AdminBikeModelSaveRequest request) {
        return ApiResponse.success(bikeModelService.saveOrUpdateModel(request));
    }

    /**
     * 修改车辆配置。
     */
    @PutMapping("/{modelId}")
    public ApiResponse<BikeModel> update(@PathVariable Long modelId,
                                         @Validated @RequestBody AdminBikeModelSaveRequest request) {
        request.setModelId(modelId);
        return ApiResponse.success(bikeModelService.saveOrUpdateModel(request));
    }

    /**
     * 删除车辆配置。
     */
    @DeleteMapping("/{modelId}")
    public ApiResponse<Void> delete(@PathVariable Long modelId) {
        bikeModelService.removeById(modelId);
        return ApiResponse.success("删除成功", null);
    }
}
