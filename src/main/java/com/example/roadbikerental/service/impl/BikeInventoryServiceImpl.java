package com.example.roadbikerental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.roadbikerental.common.exception.BusinessException;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.admin.AdminBikeInventorySaveRequest;
import com.example.roadbikerental.entity.BikeInventory;
import com.example.roadbikerental.entity.BikeModel;
import com.example.roadbikerental.entity.BikePricing;
import com.example.roadbikerental.entity.MaintenanceRecord;
import com.example.roadbikerental.entity.RentalStore;
import com.example.roadbikerental.mapper.BikeInventoryMapper;
import com.example.roadbikerental.service.BikeInventoryService;
import com.example.roadbikerental.service.BikeModelService;
import com.example.roadbikerental.service.BikePricingService;
import com.example.roadbikerental.service.RentalStoreService;
import com.example.roadbikerental.service.support.BikeMaintenanceDiscountSupport;
import com.example.roadbikerental.vo.user.UserBikeDetailVO;
import com.example.roadbikerental.vo.user.UserBikeMaintenanceVO;
import com.example.roadbikerental.vo.user.UserBikeVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 车辆库存服务实现。
 */
@Service
public class BikeInventoryServiceImpl extends ServiceImpl<BikeInventoryMapper, BikeInventory> implements BikeInventoryService {

    private final BikeModelService bikeModelService;
    private final BikePricingService bikePricingService;
    private final RentalStoreService rentalStoreService;
    private final BikeMaintenanceDiscountSupport bikeMaintenanceDiscountSupport;

    public BikeInventoryServiceImpl(BikeModelService bikeModelService,
                                    BikePricingService bikePricingService,
                                    RentalStoreService rentalStoreService,
                                    BikeMaintenanceDiscountSupport bikeMaintenanceDiscountSupport) {
        this.bikeModelService = bikeModelService;
        this.bikePricingService = bikePricingService;
        this.rentalStoreService = rentalStoreService;
        this.bikeMaintenanceDiscountSupport = bikeMaintenanceDiscountSupport;
    }

    @Override
    public PageResult<BikeInventory> pageAdminInventory(Long current, Long size, String keyword, Long storeId, Long modelId, Integer status) {
        final List<Long> matchedModelIds;
        if (StringUtils.isNotBlank(keyword)) {
            matchedModelIds = bikeModelService.list(new LambdaQueryWrapper<BikeModel>()
                    .and(wrapper -> wrapper.like(BikeModel::getBrandName, keyword)
                            .or()
                            .like(BikeModel::getSeriesName, keyword)
                            .or()
                            .like(BikeModel::getModelName, keyword)))
                    .stream()
                    .map(BikeModel::getModelId)
                    .collect(Collectors.toList());
        } else {
            matchedModelIds = Collections.emptyList();
        }

        LambdaQueryWrapper<BikeInventory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(storeId != null, BikeInventory::getCurrentStoreId, storeId)
                .eq(modelId != null, BikeInventory::getModelId, modelId)
                .eq(status != null, BikeInventory::getBikeStatus, status)
                .orderByDesc(BikeInventory::getCreatedAt);

        if (StringUtils.isNotBlank(keyword)) {
            if (matchedModelIds.isEmpty()) {
                queryWrapper.and(wrapper -> wrapper.like(BikeInventory::getBikeCode, keyword)
                        .or()
                        .like(BikeInventory::getFrameNo, keyword));
            } else {
                queryWrapper.and(wrapper -> wrapper.like(BikeInventory::getBikeCode, keyword)
                        .or()
                        .like(BikeInventory::getFrameNo, keyword)
                        .or()
                        .in(BikeInventory::getModelId, matchedModelIds));
            }
        }

        return PageResult.of(page(new Page<>(current, size), queryWrapper));
    }

    @Override
    public PageResult<UserBikeVO> pageBikes(Long current, Long size, String keyword, Long storeId, Long modelId, Integer status) {
        final List<Long> matchedModelIds;
        if (StringUtils.isNotBlank(keyword)) {
            matchedModelIds = bikeModelService.list(new LambdaQueryWrapper<BikeModel>()
                    .and(wrapper -> wrapper.like(BikeModel::getBrandName, keyword)
                            .or()
                            .like(BikeModel::getSeriesName, keyword)
                            .or()
                            .like(BikeModel::getModelName, keyword)))
                    .stream()
                    .map(BikeModel::getModelId)
                    .collect(Collectors.toList());
        } else {
            matchedModelIds = Collections.emptyList();
        }
        LambdaQueryWrapper<BikeInventory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(storeId != null, BikeInventory::getCurrentStoreId, storeId)
                .eq(modelId != null, BikeInventory::getModelId, modelId)
                .eq(status != null, BikeInventory::getBikeStatus, status)
                .orderByDesc(BikeInventory::getCreatedAt);
        if (StringUtils.isNotBlank(keyword)) {
            if (matchedModelIds.isEmpty()) {
                queryWrapper.like(BikeInventory::getBikeCode, keyword);
            } else {
                queryWrapper.and(wrapper -> wrapper.like(BikeInventory::getBikeCode, keyword)
                        .or()
                        .in(BikeInventory::getModelId, matchedModelIds));
            }
        }
        Page<BikeInventory> page = page(new Page<>(current, size), queryWrapper);
        List<UserBikeVO> records = buildBikeVOs(page.getRecords());
        Page<UserBikeVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(records);
        return PageResult.of(resultPage);
    }

    @Override
    public UserBikeDetailVO getUserBikeDetail(Long bikeId) {
        BikeInventory bikeInventory = getRequiredById(bikeId);
        List<UserBikeVO> bikes = buildBikeVOs(Collections.singletonList(bikeInventory));
        if (bikes.isEmpty()) {
            throw new BusinessException("车辆不存在");
        }

        UserBikeDetailVO detailVO = new UserBikeDetailVO();
        detailVO.setBike(bikes.get(0));
        detailVO.setMaintenanceHistory(buildUserMaintenanceVOs(bikeInventory));
        return detailVO;
    }

    @Override
    public BikeInventory saveOrUpdateInventory(AdminBikeInventorySaveRequest request) {
        BikeInventory bikeInventory = request.getBikeId() == null ? new BikeInventory() : getById(request.getBikeId());
        if (request.getBikeId() != null && bikeInventory == null) {
            throw new BusinessException("车辆不存在");
        }

        if (bikeModelService.getById(request.getModelId()) == null) {
            throw new BusinessException("车辆配置不存在");
        }
        if (rentalStoreService.getById(request.getCurrentStoreId()) == null) {
            throw new BusinessException("门店不存在");
        }
        if (count(new LambdaQueryWrapper<BikeInventory>()
                .eq(BikeInventory::getBikeCode, request.getBikeCode())
                .ne(request.getBikeId() != null, BikeInventory::getBikeId, request.getBikeId())) > 0) {
            throw new BusinessException("车辆编码已存在");
        }
        if (count(new LambdaQueryWrapper<BikeInventory>()
                .eq(BikeInventory::getFrameNo, request.getFrameNo())
                .ne(request.getBikeId() != null, BikeInventory::getBikeId, request.getBikeId())) > 0) {
            throw new BusinessException("车架编号已存在");
        }

        BeanUtils.copyProperties(request, bikeInventory);
        if (bikeInventory.getMileageKm() == null) {
            bikeInventory.setMileageKm(java.math.BigDecimal.ZERO);
        }
        saveOrUpdate(bikeInventory);
        return bikeInventory;
    }

    @Override
    public BikeInventory getRequiredById(Long bikeId) {
        BikeInventory bikeInventory = getById(bikeId);
        if (bikeInventory == null) {
            throw new BusinessException("车辆不存在");
        }
        return bikeInventory;
    }

    private List<UserBikeVO> buildBikeVOs(List<BikeInventory> bikes) {
        if (bikes.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> modelIds = bikes.stream().map(BikeInventory::getModelId).collect(Collectors.toSet());
        Set<Long> storeIds = bikes.stream().map(BikeInventory::getCurrentStoreId).collect(Collectors.toSet());
        Set<Long> bikeIds = bikes.stream().map(BikeInventory::getBikeId).collect(Collectors.toSet());
        Map<Long, BikeModel> modelMap = bikeModelService.listByIds(modelIds).stream()
                .collect(Collectors.toMap(BikeModel::getModelId, Function.identity()));
        Map<Long, RentalStore> storeMap = rentalStoreService.listByIds(storeIds).stream()
                .collect(Collectors.toMap(RentalStore::getStoreId, Function.identity()));
        Map<Long, Long> maintenanceHistoryCountMap = bikeMaintenanceDiscountSupport.countCompletedHistoryByBikeIds(bikeIds);
        return bikes.stream().map(bike -> {
            BikeModel bikeModel = modelMap.get(bike.getModelId());
            RentalStore store = storeMap.get(bike.getCurrentStoreId());
            BikePricing pricing = bikePricingService.getCurrentPricingByModelId(bike.getModelId());
            long maintenanceHistoryCount = maintenanceHistoryCountMap.getOrDefault(bike.getBikeId(), 0L);
            UserBikeVO vo = new UserBikeVO();
            vo.setBikeId(bike.getBikeId());
            vo.setBikeCode(bike.getBikeCode());
            vo.setModelId(bike.getModelId());
            vo.setFrameSize(bike.getFrameSize());
            vo.setColor(bike.getColor());
            vo.setBikeStatus(bike.getBikeStatus());
            vo.setMileageKm(bike.getMileageKm());
            if (bikeModel != null) {
                vo.setBrandName(bikeModel.getBrandName());
                vo.setModelName(bikeModel.getModelName());
                vo.setBikeType(bikeModel.getBikeType());
                vo.setFrameMaterial(bikeModel.getFrameMaterial());
                vo.setGearSystem(bikeModel.getGearSystem());
                vo.setBrakeType(bikeModel.getBrakeType());
                vo.setWheelSize(bikeModel.getWheelSize());
                vo.setBikeWeight(bikeModel.getBikeWeight());
                vo.setCoverImageUrl(bikeModel.getCoverImageUrl());
                vo.setDescription(bikeModel.getDescription());
            }
            if (store != null) {
                vo.setStoreId(store.getStoreId());
                vo.setStoreName(store.getStoreName());
            }
            if (pricing != null) {
                vo.setOriginalDailyRent(pricing.getDailyRent());
                vo.setMaintenanceDiscountRate(
                        bikeMaintenanceDiscountSupport.resolveDiscountRate(maintenanceHistoryCount));
                vo.setDailyRent(
                        bikeMaintenanceDiscountSupport.applyDiscount(pricing.getDailyRent(), maintenanceHistoryCount));
                vo.setDepositAmount(pricing.getDepositAmount());
            }
            vo.setMaintenanceHistoryCount(Math.toIntExact(maintenanceHistoryCount));
            return vo;
        }).collect(Collectors.toList());
    }

    private List<UserBikeMaintenanceVO> buildUserMaintenanceVOs(BikeInventory bikeInventory) {
        List<MaintenanceRecord> records = bikeMaintenanceDiscountSupport.listBikeHistory(bikeInventory.getBikeId());
        if (records.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, RentalStore> storeMap = rentalStoreService.listByIds(records.stream()
                        .map(MaintenanceRecord::getStoreId)
                        .filter(id -> id != null)
                        .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(RentalStore::getStoreId, Function.identity()));

        return records.stream().map(record -> {
            UserBikeMaintenanceVO vo = new UserBikeMaintenanceVO();
            vo.setMaintenanceId(record.getMaintenanceId());
            vo.setBikeId(record.getBikeId());
            vo.setBikeCode(bikeInventory.getBikeCode());
            vo.setStoreId(record.getStoreId());
            RentalStore store = record.getStoreId() == null ? null : storeMap.get(record.getStoreId());
            if (store != null) {
                vo.setStoreName(store.getStoreName());
            }
            vo.setMaintenanceType(record.getMaintenanceType());
            vo.setFaultDescription(record.getFaultDescription());
            vo.setMaintenanceContent(record.getMaintenanceContent());
            vo.setMaintenanceCost(record.getMaintenanceCost());
            vo.setStartTime(record.getStartTime());
            vo.setEndTime(record.getEndTime());
            vo.setMaintenanceStatus(record.getMaintenanceStatus());
            vo.setNextMaintenanceDate(record.getNextMaintenanceDate());
            vo.setRemark(record.getRemark());
            vo.setCreatedAt(record.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
    }
}
