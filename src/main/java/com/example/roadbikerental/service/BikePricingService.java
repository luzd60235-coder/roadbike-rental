package com.example.roadbikerental.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.admin.AdminPricingSaveRequest;
import com.example.roadbikerental.entity.BikePricing;

/**
 * 价格策略服务。
 */
public interface BikePricingService extends IService<BikePricing> {

    PageResult<BikePricing> pagePricing(Long current, Long size, Long modelId, Integer status);

    BikePricing saveOrUpdatePricing(AdminPricingSaveRequest request);

    BikePricing getCurrentPricingByModelId(Long modelId);
}
