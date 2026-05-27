package com.example.roadbikerental.service.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.roadbikerental.common.enums.MaintenanceStatusEnum;
import com.example.roadbikerental.entity.MaintenanceRecord;
import com.example.roadbikerental.mapper.MaintenanceRecordMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Calculates maintenance-history based rental discounts.
 */
@Component
public class BikeMaintenanceDiscountSupport {

    private static final BigDecimal STEP_RATE = new BigDecimal("0.05");
    private static final BigDecimal MAX_RATE = new BigDecimal("0.15");

    private final MaintenanceRecordMapper maintenanceRecordMapper;

    public BikeMaintenanceDiscountSupport(MaintenanceRecordMapper maintenanceRecordMapper) {
        this.maintenanceRecordMapper = maintenanceRecordMapper;
    }

    public Map<Long, Long> countCompletedHistoryByBikeIds(Collection<Long> bikeIds) {
        if (bikeIds == null || bikeIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return maintenanceRecordMapper.selectList(new LambdaQueryWrapper<MaintenanceRecord>()
                        .select(MaintenanceRecord::getBikeId)
                        .in(MaintenanceRecord::getBikeId, bikeIds)
                        .eq(MaintenanceRecord::getMaintenanceStatus, MaintenanceStatusEnum.COMPLETED.getCode()))
                .stream()
                .filter(item -> item.getBikeId() != null)
                .collect(Collectors.groupingBy(MaintenanceRecord::getBikeId, Collectors.counting()));
    }

    public long countCompletedHistory(Long bikeId) {
        if (bikeId == null) {
            return 0L;
        }
        Long count = maintenanceRecordMapper.selectCount(new LambdaQueryWrapper<MaintenanceRecord>()
                .eq(MaintenanceRecord::getBikeId, bikeId)
                .eq(MaintenanceRecord::getMaintenanceStatus, MaintenanceStatusEnum.COMPLETED.getCode()));
        return count == null ? 0L : count;
    }

    public BigDecimal resolveDiscountRate(long completedHistoryCount) {
        if (completedHistoryCount <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal rate = STEP_RATE.multiply(BigDecimal.valueOf(completedHistoryCount));
        if (rate.compareTo(MAX_RATE) > 0) {
            rate = MAX_RATE;
        }
        return rate.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal applyDiscount(BigDecimal originalDailyRent, long completedHistoryCount) {
        return applyDiscount(originalDailyRent, resolveDiscountRate(completedHistoryCount));
    }

    public BigDecimal applyDiscount(BigDecimal originalDailyRent, BigDecimal discountRate) {
        if (originalDailyRent == null) {
            return null;
        }
        BigDecimal safeRate = discountRate == null ? BigDecimal.ZERO : discountRate;
        return originalDailyRent.multiply(BigDecimal.ONE.subtract(safeRate)).setScale(2, RoundingMode.HALF_UP);
    }

    public List<MaintenanceRecord> listBikeHistory(Long bikeId) {
        if (bikeId == null) {
            return Collections.emptyList();
        }
        return maintenanceRecordMapper.selectList(new LambdaQueryWrapper<MaintenanceRecord>()
                .eq(MaintenanceRecord::getBikeId, bikeId)
                .orderByDesc(MaintenanceRecord::getCreatedAt)
                .orderByDesc(MaintenanceRecord::getMaintenanceId));
    }
}
