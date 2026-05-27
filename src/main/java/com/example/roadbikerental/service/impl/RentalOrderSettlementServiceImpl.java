package com.example.roadbikerental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.roadbikerental.common.enums.BikeStatusEnum;
import com.example.roadbikerental.common.enums.OrderStatusEnum;
import com.example.roadbikerental.common.exception.BusinessException;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.staff.StaffSettlementCreateRequest;
import com.example.roadbikerental.entity.BikePricing;
import com.example.roadbikerental.entity.BikeInventory;
import com.example.roadbikerental.entity.RentalOrder;
import com.example.roadbikerental.entity.RentalOrderSettlement;
import com.example.roadbikerental.entity.RentalStore;
import com.example.roadbikerental.entity.StoreStaff;
import com.example.roadbikerental.mapper.RentalOrderSettlementMapper;
import com.example.roadbikerental.service.BikeInventoryService;
import com.example.roadbikerental.service.BikePricingService;
import com.example.roadbikerental.service.RentalOrderService;
import com.example.roadbikerental.service.RentalOrderSettlementService;
import com.example.roadbikerental.service.RentalStoreService;
import com.example.roadbikerental.service.StoreStaffService;
import com.example.roadbikerental.vo.staff.SettlementVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 订单结算服务实现。
 */
@Service
public class RentalOrderSettlementServiceImpl extends ServiceImpl<RentalOrderSettlementMapper, RentalOrderSettlement>
        implements RentalOrderSettlementService {

    private final RentalOrderService rentalOrderService;
    private final BikeInventoryService bikeInventoryService;
    private final BikePricingService bikePricingService;
    private final RentalStoreService rentalStoreService;
    private final StoreStaffService storeStaffService;

    public RentalOrderSettlementServiceImpl(RentalOrderService rentalOrderService,
                                            BikeInventoryService bikeInventoryService,
                                            BikePricingService bikePricingService,
                                            RentalStoreService rentalStoreService,
                                            StoreStaffService storeStaffService) {
        this.rentalOrderService = rentalOrderService;
        this.bikeInventoryService = bikeInventoryService;
        this.bikePricingService = bikePricingService;
        this.rentalStoreService = rentalStoreService;
        this.storeStaffService = storeStaffService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SettlementVO createSettlement(Long staffId, Long storeId, StaffSettlementCreateRequest request) {
        RentalOrder order = rentalOrderService.getById(request.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!Integer.valueOf(OrderStatusEnum.PENDING_SETTLEMENT.getCode()).equals(order.getOrderStatus())) {
            throw new BusinessException("当前订单不可结算");
        }
        if (order.getReturnStoreId() == null || !storeId.equals(order.getReturnStoreId())) {
            throw new BusinessException("只能结算本门店归还的订单");
        }
        if (count(new LambdaQueryWrapper<RentalOrderSettlement>()
                .eq(RentalOrderSettlement::getOrderId, request.getOrderId())) > 0) {
            throw new BusinessException("该订单已完成结算");
        }

        BigDecimal baseRentAmount = computeBaseRentAmount(order);
        BigDecimal overtimeAmount = computeOvertimeAmount(order);
        BigDecimal extraCharge = overtimeAmount
                .add(request.getMaintenanceCompensation())
                .add(request.getLossCompensation())
                .subtract(request.getDiscountAmount());
        if (extraCharge.compareTo(BigDecimal.ZERO) < 0) {
            extraCharge = BigDecimal.ZERO;
        }
        BigDecimal depositPaid = order.getDepositAmount();
        BigDecimal refundDepositAmount = depositPaid.subtract(extraCharge);
        if (refundDepositAmount.compareTo(BigDecimal.ZERO) < 0) {
            refundDepositAmount = BigDecimal.ZERO;
        }

        RentalOrderSettlement settlement = new RentalOrderSettlement();
        settlement.setOrderId(order.getOrderId());
        settlement.setSettleStoreId(storeId);
        settlement.setSettleStaffId(staffId);
        settlement.setBaseRentAmount(baseRentAmount);
        settlement.setOvertimeAmount(overtimeAmount);
        settlement.setMaintenanceCompensation(request.getMaintenanceCompensation());
        settlement.setLossCompensation(request.getLossCompensation());
        settlement.setDiscountAmount(request.getDiscountAmount());
        settlement.setPayableTotal(baseRentAmount.add(extraCharge));
        settlement.setDepositPaid(depositPaid);
        settlement.setRefundDepositAmount(refundDepositAmount);
        settlement.setSettlementNote(request.getSettlementNote());
        settlement.setSettledAt(LocalDateTime.now());
        save(settlement);

        order.setOvertimeAmount(overtimeAmount);
        order.setDamageAmount(request.getMaintenanceCompensation().add(request.getLossCompensation()));
        order.setOtherAmount(BigDecimal.ZERO);
        order.setTotalAmount(order.getRentAmount()
                .add(order.getDepositAmount())
                .add(overtimeAmount)
                .add(request.getMaintenanceCompensation())
                .add(request.getLossCompensation())
                .subtract(request.getDiscountAmount()));
        order.setOrderStatus(OrderStatusEnum.COMPLETED.getCode());
        order.setPaymentStatus(refundDepositAmount.compareTo(BigDecimal.ZERO) > 0 ? 3 : 1);
        if (refundDepositAmount.compareTo(depositPaid) == 0) {
            order.setDepositRefundStatus(2);
        } else if (refundDepositAmount.compareTo(BigDecimal.ZERO) > 0) {
            order.setDepositRefundStatus(1);
        } else {
            order.setDepositRefundStatus(0);
        }
        rentalOrderService.updateById(order);

        BikeInventory bikeInventory = bikeInventoryService.getRequiredById(order.getBikeId());
        bikeInventory.setBikeStatus(BikeStatusEnum.AVAILABLE.getCode());
        bikeInventoryService.updateById(bikeInventory);
        return buildVOs(Collections.singletonList(settlement)).get(0);
    }

    private BigDecimal computeBaseRentAmount(RentalOrder order) {
        return order.getRentAmount() == null ? BigDecimal.ZERO : order.getRentAmount();
    }

    private BigDecimal computeOvertimeAmount(RentalOrder order) {
        if (order.getPlannedEndTime() == null || order.getActualReturnTime() == null
                || !order.getActualReturnTime().isAfter(order.getPlannedEndTime())) {
            return BigDecimal.ZERO;
        }

        BikePricing pricing = order.getPricingId() == null ? null : bikePricingService.getById(order.getPricingId());
        if (pricing == null || pricing.getOvertimeFeePerHour() == null) {
            return BigDecimal.ZERO;
        }

        long overtimeMinutes = ChronoUnit.MINUTES.between(order.getPlannedEndTime(), order.getActualReturnTime());
        long overtimeHours = Math.max(1L, (long) Math.ceil(overtimeMinutes / 60D));
        return pricing.getOvertimeFeePerHour().multiply(BigDecimal.valueOf(overtimeHours));
    }

    @Override
    public PageResult<SettlementVO> pageStoreSettlements(Long current, Long size, Long storeId) {
        Page<RentalOrderSettlement> page = page(new Page<>(current, size), new LambdaQueryWrapper<RentalOrderSettlement>()
                .eq(RentalOrderSettlement::getSettleStoreId, storeId)
                .orderByDesc(RentalOrderSettlement::getSettledAt));
        List<SettlementVO> records = buildVOs(page.getRecords());
        Page<SettlementVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(records);
        return PageResult.of(resultPage);
    }

    private List<SettlementVO> buildVOs(List<RentalOrderSettlement> settlements) {
        if (settlements.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> orderIds = settlements.stream().map(RentalOrderSettlement::getOrderId).collect(Collectors.toSet());
        Set<Long> storeIds = settlements.stream().map(RentalOrderSettlement::getSettleStoreId).collect(Collectors.toSet());
        Set<Long> staffIds = settlements.stream().map(RentalOrderSettlement::getSettleStaffId).collect(Collectors.toSet());

        Map<Long, RentalOrder> orderMap = rentalOrderService.listByIds(orderIds).stream()
                .collect(Collectors.toMap(RentalOrder::getOrderId, Function.identity()));
        Map<Long, RentalStore> storeMap = rentalStoreService.listByIds(storeIds).stream()
                .collect(Collectors.toMap(RentalStore::getStoreId, Function.identity()));
        Map<Long, StoreStaff> staffMap = storeStaffService.listByIds(staffIds).stream()
                .collect(Collectors.toMap(StoreStaff::getStaffId, Function.identity()));

        return settlements.stream().map(settlement -> {
            SettlementVO vo = new SettlementVO();
            vo.setSettlementId(settlement.getSettlementId());
            vo.setOrderId(settlement.getOrderId());
            RentalOrder order = orderMap.get(settlement.getOrderId());
            if (order != null) {
                vo.setOrderNo(order.getOrderNo());
            }
            vo.setSettleStoreId(settlement.getSettleStoreId());
            RentalStore store = storeMap.get(settlement.getSettleStoreId());
            if (store != null) {
                vo.setSettleStoreName(store.getStoreName());
            }
            vo.setSettleStaffId(settlement.getSettleStaffId());
            StoreStaff staff = staffMap.get(settlement.getSettleStaffId());
            if (staff != null) {
                vo.setSettleStaffName(staff.getStaffName());
            }
            vo.setBaseRentAmount(settlement.getBaseRentAmount());
            vo.setOvertimeAmount(settlement.getOvertimeAmount());
            vo.setMaintenanceCompensation(settlement.getMaintenanceCompensation());
            vo.setLossCompensation(settlement.getLossCompensation());
            vo.setDiscountAmount(settlement.getDiscountAmount());
            vo.setPayableTotal(settlement.getPayableTotal());
            vo.setDepositPaid(settlement.getDepositPaid());
            vo.setRefundDepositAmount(settlement.getRefundDepositAmount());
            vo.setSettlementNote(settlement.getSettlementNote());
            vo.setSettledAt(settlement.getSettledAt());
            return vo;
        }).collect(Collectors.toList());
    }
}
