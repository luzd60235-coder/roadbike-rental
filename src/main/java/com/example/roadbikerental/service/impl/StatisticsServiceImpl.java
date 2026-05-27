package com.example.roadbikerental.service.impl;

import com.example.roadbikerental.entity.FinancialOverview;
import com.example.roadbikerental.mapper.FinancialOverviewMapper;
import com.example.roadbikerental.service.AppUserService;
import com.example.roadbikerental.service.BikeInventoryService;
import com.example.roadbikerental.service.BikeModelService;
import com.example.roadbikerental.service.RentalStoreService;
import com.example.roadbikerental.service.StatisticsService;
import com.example.roadbikerental.vo.admin.AdminStatisticsVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 后台统计服务实现。
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final AppUserService appUserService;
    private final RentalStoreService rentalStoreService;
    private final BikeModelService bikeModelService;
    private final BikeInventoryService bikeInventoryService;
    private final FinancialOverviewMapper financialOverviewMapper;

    public StatisticsServiceImpl(AppUserService appUserService,
                                 RentalStoreService rentalStoreService,
                                 BikeModelService bikeModelService,
                                 BikeInventoryService bikeInventoryService,
                                 FinancialOverviewMapper financialOverviewMapper) {
        this.appUserService = appUserService;
        this.rentalStoreService = rentalStoreService;
        this.bikeModelService = bikeModelService;
        this.bikeInventoryService = bikeInventoryService;
        this.financialOverviewMapper = financialOverviewMapper;
    }

    @Override
    public AdminStatisticsVO getOverview() {
        FinancialOverview overview = financialOverviewMapper.selectOverview();
        AdminStatisticsVO vo = new AdminStatisticsVO();
        vo.setTotalUsers(appUserService.count());
        vo.setTotalStores(rentalStoreService.count());
        vo.setTotalBikeModels(bikeModelService.count());
        vo.setTotalBikes(bikeInventoryService.count());
        vo.setTotalOrders(overview == null ? 0L : overview.getTotalOrders());
        vo.setTotalRentIncome(overview == null || overview.getTotalRentIncome() == null ? BigDecimal.ZERO : overview.getTotalRentIncome());
        vo.setTotalDepositCollected(overview == null || overview.getTotalDepositCollected() == null ? BigDecimal.ZERO : overview.getTotalDepositCollected());
        vo.setTotalDepositRefunded(overview == null || overview.getTotalDepositRefunded() == null ? BigDecimal.ZERO : overview.getTotalDepositRefunded());
        vo.setTotalExtraIncome(overview == null || overview.getTotalExtraIncome() == null ? BigDecimal.ZERO : overview.getTotalExtraIncome());
        vo.setTotalMaintenanceCost(overview == null || overview.getTotalMaintenanceCost() == null ? BigDecimal.ZERO : overview.getTotalMaintenanceCost());
        vo.setEstimatedNetIncome(overview == null || overview.getEstimatedNetIncome() == null ? BigDecimal.ZERO : overview.getEstimatedNetIncome());
        return vo;
    }
}
