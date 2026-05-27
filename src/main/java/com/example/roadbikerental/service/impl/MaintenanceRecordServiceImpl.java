package com.example.roadbikerental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.roadbikerental.common.enums.BikeStatusEnum;
import com.example.roadbikerental.common.enums.MaintenanceStatusEnum;
import com.example.roadbikerental.common.exception.BusinessException;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.staff.StaffMaintenanceSaveRequest;
import com.example.roadbikerental.dto.staff.StaffMaintenanceUpdateRequest;
import com.example.roadbikerental.entity.BikeInventory;
import com.example.roadbikerental.entity.MaintenanceRecord;
import com.example.roadbikerental.entity.RentalStore;
import com.example.roadbikerental.entity.StoreStaff;
import com.example.roadbikerental.mapper.MaintenanceRecordMapper;
import com.example.roadbikerental.service.BikeInventoryService;
import com.example.roadbikerental.service.MaintenanceRecordService;
import com.example.roadbikerental.service.RentalStoreService;
import com.example.roadbikerental.service.StoreStaffService;
import com.example.roadbikerental.vo.staff.MaintenanceVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 维修记录服务实现。
 */
@Service
public class MaintenanceRecordServiceImpl extends ServiceImpl<MaintenanceRecordMapper, MaintenanceRecord>
        implements MaintenanceRecordService {

    private final BikeInventoryService bikeInventoryService;
    private final RentalStoreService rentalStoreService;
    private final StoreStaffService storeStaffService;

    public MaintenanceRecordServiceImpl(BikeInventoryService bikeInventoryService,
                                        RentalStoreService rentalStoreService,
                                        StoreStaffService storeStaffService) {
        this.bikeInventoryService = bikeInventoryService;
        this.rentalStoreService = rentalStoreService;
        this.storeStaffService = storeStaffService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaintenanceVO createRecord(Long staffId, Long storeId, StaffMaintenanceSaveRequest request) {
        BikeInventory bikeInventory = bikeInventoryService.getRequiredById(request.getBikeId());
        MaintenanceRecord record = new MaintenanceRecord();
        BeanUtils.copyProperties(request, record);
        record.setStoreId(storeId);
        record.setReportedByStaffId(staffId);
        record.setMaintenanceStatus(request.getMaintenanceStatus() == null
                ? MaintenanceStatusEnum.PENDING.getCode() : request.getMaintenanceStatus());
        record.setMaintenanceCost(request.getMaintenanceCost() == null ? BigDecimal.ZERO : request.getMaintenanceCost());
        applyLifecycleDefaults(record);
        save(record);

        bikeInventory.setBikeStatus(BikeStatusEnum.MAINTENANCE.getCode());
        bikeInventoryService.updateById(bikeInventory);
        return buildVOs(Collections.singletonList(record)).get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaintenanceVO updateRecord(Long maintenanceId, Long storeId, StaffMaintenanceUpdateRequest request) {
        MaintenanceRecord record = getById(maintenanceId);
        if (record == null) {
            throw new BusinessException("维修记录不存在");
        }
        if (!storeId.equals(record.getStoreId())) {
            throw new BusinessException("只能修改本门店维修记录");
        }

        if (request.getAssignedStaffId() != null) {
            record.setAssignedStaffId(request.getAssignedStaffId());
        }
        if (request.getMaintenanceContent() != null) {
            record.setMaintenanceContent(request.getMaintenanceContent());
        }
        if (request.getMaintenanceCost() != null) {
            record.setMaintenanceCost(request.getMaintenanceCost());
        }
        if (request.getStartTime() != null) {
            record.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            record.setEndTime(request.getEndTime());
        }
        if (request.getMaintenanceStatus() != null) {
            record.setMaintenanceStatus(request.getMaintenanceStatus());
        }
        if (request.getNextMaintenanceDate() != null) {
            record.setNextMaintenanceDate(request.getNextMaintenanceDate());
        }
        if (request.getRemark() != null) {
            record.setRemark(request.getRemark());
        }
        applyLifecycleDefaults(record);
        updateById(record);

        BikeInventory bikeInventory = bikeInventoryService.getRequiredById(record.getBikeId());
        if (Integer.valueOf(MaintenanceStatusEnum.COMPLETED.getCode()).equals(record.getMaintenanceStatus())) {
            bikeInventory.setLastMaintenanceAt(record.getEndTime() == null ? LocalDateTime.now() : record.getEndTime());
            bikeInventory.setBikeStatus(BikeStatusEnum.AVAILABLE.getCode());
        } else {
            bikeInventory.setBikeStatus(BikeStatusEnum.MAINTENANCE.getCode());
        }
        bikeInventoryService.updateById(bikeInventory);
        return buildVOs(Collections.singletonList(record)).get(0);
    }

    private void applyLifecycleDefaults(MaintenanceRecord record) {
        if (Integer.valueOf(MaintenanceStatusEnum.PROCESSING.getCode()).equals(record.getMaintenanceStatus())
                && record.getStartTime() == null) {
            record.setStartTime(LocalDateTime.now());
        }

        if (Integer.valueOf(MaintenanceStatusEnum.COMPLETED.getCode()).equals(record.getMaintenanceStatus())
                && record.getEndTime() == null) {
            record.setEndTime(LocalDateTime.now());
        }
    }

    @Override
    public PageResult<MaintenanceVO> pageRecords(Long current, Long size, Long storeId, Integer status) {
        Page<MaintenanceRecord> page = page(new Page<>(current, size), new LambdaQueryWrapper<MaintenanceRecord>()
                .eq(MaintenanceRecord::getStoreId, storeId)
                .eq(status != null, MaintenanceRecord::getMaintenanceStatus, status)
                .orderByDesc(MaintenanceRecord::getCreatedAt));
        List<MaintenanceVO> records = buildVOs(page.getRecords());
        Page<MaintenanceVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(records);
        return PageResult.of(resultPage);
    }

    private List<MaintenanceVO> buildVOs(List<MaintenanceRecord> records) {
        if (records.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> bikeIds = records.stream().map(MaintenanceRecord::getBikeId).collect(Collectors.toSet());
        Set<Long> storeIds = records.stream().map(MaintenanceRecord::getStoreId).collect(Collectors.toSet());
        Set<Long> staffIds = records.stream().map(MaintenanceRecord::getAssignedStaffId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Long, BikeInventory> bikeMap = bikeInventoryService.listByIds(bikeIds).stream()
                .collect(Collectors.toMap(BikeInventory::getBikeId, Function.identity()));
        Map<Long, RentalStore> storeMap = rentalStoreService.listByIds(storeIds).stream()
                .collect(Collectors.toMap(RentalStore::getStoreId, Function.identity()));
        Map<Long, StoreStaff> staffMap = staffIds.isEmpty()
                ? Collections.emptyMap()
                : storeStaffService.listByIds(staffIds).stream()
                .collect(Collectors.toMap(StoreStaff::getStaffId, Function.identity()));

        return records.stream().map(record -> {
            MaintenanceVO vo = new MaintenanceVO();
            vo.setMaintenanceId(record.getMaintenanceId());
            vo.setBikeId(record.getBikeId());
            BikeInventory bikeInventory = bikeMap.get(record.getBikeId());
            if (bikeInventory != null) {
                vo.setBikeCode(bikeInventory.getBikeCode());
            }
            vo.setStoreId(record.getStoreId());
            RentalStore store = storeMap.get(record.getStoreId());
            if (store != null) {
                vo.setStoreName(store.getStoreName());
            }
            vo.setReportedByStaffId(record.getReportedByStaffId());
            vo.setAssignedStaffId(record.getAssignedStaffId());
            if (record.getAssignedStaffId() != null) {
                StoreStaff staff = staffMap.get(record.getAssignedStaffId());
                if (staff != null) {
                    vo.setAssignedStaffName(staff.getStaffName());
                }
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
