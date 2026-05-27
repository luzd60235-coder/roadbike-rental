package com.example.roadbikerental.controller.admin;

import com.example.roadbikerental.common.annotation.RoleRequired;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.result.ApiResponse;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.admin.AdminPricingSaveRequest;
import com.example.roadbikerental.entity.BikePricing;
import com.example.roadbikerental.service.BikePricingService;
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
 * 后台价格管理控制器。
 */
@RestController
@RequestMapping("/api/admin/pricings")
@RoleRequired(RoleType.ADMIN)
public class AdminPricingController {

    private final BikePricingService bikePricingService;

    public AdminPricingController(BikePricingService bikePricingService) {
        this.bikePricingService = bikePricingService;
    }

    /**
     * 价格策略分页查询。
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<BikePricing>> page(@RequestParam(defaultValue = "1") Long current,
                                                     @RequestParam(defaultValue = "10") Long size,
                                                     @RequestParam(required = false) Long modelId,
                                                     @RequestParam(required = false) Integer status) {
        return ApiResponse.success(bikePricingService.pagePricing(current, size, modelId, status));
    }

    /**
     * 查询价格策略详情。
     */
    @GetMapping("/{pricingId}")
    public ApiResponse<BikePricing> detail(@PathVariable Long pricingId) {
        return ApiResponse.success(bikePricingService.getById(pricingId));
    }

    /**
     * 新增价格策略。
     */
    @PostMapping
    public ApiResponse<BikePricing> save(@Validated @RequestBody AdminPricingSaveRequest request) {
        return ApiResponse.success(bikePricingService.saveOrUpdatePricing(request));
    }

    /**
     * 修改价格策略。
     */
    @PutMapping("/{pricingId}")
    public ApiResponse<BikePricing> update(@PathVariable Long pricingId,
                                           @Validated @RequestBody AdminPricingSaveRequest request) {
        request.setPricingId(pricingId);
        return ApiResponse.success(bikePricingService.saveOrUpdatePricing(request));
    }

    /**
     * 删除价格策略。
     */
    @DeleteMapping("/{pricingId}")
    public ApiResponse<Void> delete(@PathVariable Long pricingId) {
        bikePricingService.removeById(pricingId);
        return ApiResponse.success("删除成功", null);
    }
}
