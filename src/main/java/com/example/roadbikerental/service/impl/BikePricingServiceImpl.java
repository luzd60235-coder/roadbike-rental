package com.example.roadbikerental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.roadbikerental.common.exception.BusinessException;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.admin.AdminPricingSaveRequest;
import com.example.roadbikerental.entity.BikePricing;
import com.example.roadbikerental.mapper.BikePricingMapper;
import com.example.roadbikerental.service.BikePricingService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 价格策略服务实现。
 */
@Service
public class BikePricingServiceImpl extends ServiceImpl<BikePricingMapper, BikePricing> implements BikePricingService {

    @Override
    public PageResult<BikePricing> pagePricing(Long current, Long size, Long modelId, Integer status) {
        Page<BikePricing> page = page(new Page<>(current, size), new LambdaQueryWrapper<BikePricing>()
                .eq(modelId != null, BikePricing::getModelId, modelId)
                .eq(status != null, BikePricing::getStatus, status)
                .orderByDesc(BikePricing::getEffectiveFrom)
                .orderByDesc(BikePricing::getCreatedAt));
        return PageResult.of(page);
    }

    @Override
    public BikePricing saveOrUpdatePricing(AdminPricingSaveRequest request) {
        BikePricing pricing = request.getPricingId() == null ? new BikePricing() : getById(request.getPricingId());
        if (request.getPricingId() != null && pricing == null) {
            throw new BusinessException("价格策略不存在");
        }
        BeanUtils.copyProperties(request, pricing);
        saveOrUpdate(pricing);
        return pricing;
    }

    @Override
    public BikePricing getCurrentPricingByModelId(Long modelId) {
        List<BikePricing> list = list(new LambdaQueryWrapper<BikePricing>()
                .eq(BikePricing::getModelId, modelId)
                .eq(BikePricing::getStatus, 1)
                .le(BikePricing::getEffectiveFrom, LocalDateTime.now())
                .and(wrapper -> wrapper.isNull(BikePricing::getEffectiveTo).or().ge(BikePricing::getEffectiveTo, LocalDateTime.now()))
                .orderByDesc(BikePricing::getEffectiveFrom)
                .last("limit 1"));
        return list.isEmpty() ? null : list.get(0);
    }
}
