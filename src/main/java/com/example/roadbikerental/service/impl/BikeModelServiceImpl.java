package com.example.roadbikerental.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.roadbikerental.common.enums.BikeStatusEnum;
import com.example.roadbikerental.common.exception.BusinessException;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.admin.AdminBikeModelSaveRequest;
import com.example.roadbikerental.entity.BikeInventory;
import com.example.roadbikerental.entity.BikeModel;
import com.example.roadbikerental.mapper.BikeInventoryMapper;
import com.example.roadbikerental.mapper.BikeModelMapper;
import com.example.roadbikerental.service.BikeModelService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 车辆配置服务实现。
 */
@Service
public class BikeModelServiceImpl extends ServiceImpl<BikeModelMapper, BikeModel> implements BikeModelService {

    private final BikeInventoryMapper bikeInventoryMapper;

    public BikeModelServiceImpl(BikeInventoryMapper bikeInventoryMapper) {
        this.bikeInventoryMapper = bikeInventoryMapper;
    }

    @Override
    public PageResult<BikeModel> pageModels(Long current, Long size, String keyword, Integer status) {
        LambdaQueryWrapper<BikeModel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(status != null, BikeModel::getStatus, status)
                .and(StringUtils.isNotBlank(keyword), wrapper -> wrapper
                        .like(BikeModel::getBrandName, keyword)
                        .or()
                        .like(BikeModel::getSeriesName, keyword)
                        .or()
                        .like(BikeModel::getModelName, keyword))
                .orderByDesc(BikeModel::getCreatedAt);
        return PageResult.of(page(new Page<>(current, size), queryWrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BikeModel saveOrUpdateModel(AdminBikeModelSaveRequest request) {
        BikeModel bikeModel = request.getModelId() == null ? new BikeModel() : getById(request.getModelId());
        if (request.getModelId() != null && bikeModel == null) {
            throw new BusinessException("车辆配置不存在");
        }
        BeanUtils.copyProperties(request, bikeModel);
        saveOrUpdate(bikeModel);
        syncInventoryStatusWhenModelDisabled(bikeModel);
        return bikeModel;
    }

    private void syncInventoryStatusWhenModelDisabled(BikeModel bikeModel) {
        if (!Integer.valueOf(0).equals(bikeModel.getStatus()) || bikeModel.getModelId() == null) {
            return;
        }

        bikeInventoryMapper.update(null, new LambdaUpdateWrapper<BikeInventory>()
                .eq(BikeInventory::getModelId, bikeModel.getModelId())
                .eq(BikeInventory::getBikeStatus, BikeStatusEnum.AVAILABLE.getCode())
                .set(BikeInventory::getBikeStatus, BikeStatusEnum.DISABLED.getCode()));
    }
}
