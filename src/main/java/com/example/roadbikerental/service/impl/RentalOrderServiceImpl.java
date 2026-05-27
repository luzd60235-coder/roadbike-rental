package com.example.roadbikerental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.roadbikerental.common.enums.BikeStatusEnum;
import com.example.roadbikerental.common.enums.OrderStatusEnum;
import com.example.roadbikerental.common.exception.BusinessException;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.common.util.OrderNoGenerator;
import com.example.roadbikerental.dto.admin.AdminOrderUpdateRequest;
import com.example.roadbikerental.dto.staff.StaffOfflineOrderCreateRequest;
import com.example.roadbikerental.dto.staff.StaffPickupRequest;
import com.example.roadbikerental.dto.staff.StaffReturnRequest;
import com.example.roadbikerental.dto.user.UserOrderCreateRequest;
import com.example.roadbikerental.entity.AppUser;
import com.example.roadbikerental.entity.BikeInventory;
import com.example.roadbikerental.entity.BikeModel;
import com.example.roadbikerental.entity.BikePricing;
import com.example.roadbikerental.entity.RentalOrder;
import com.example.roadbikerental.entity.RentalStore;
import com.example.roadbikerental.entity.StoreStaff;
import com.example.roadbikerental.mapper.RentalOrderMapper;
import com.example.roadbikerental.service.AppUserService;
import com.example.roadbikerental.service.BikeInventoryService;
import com.example.roadbikerental.service.BikeModelService;
import com.example.roadbikerental.service.BikePricingService;
import com.example.roadbikerental.service.RentalOrderService;
import com.example.roadbikerental.service.RentalStoreService;
import com.example.roadbikerental.service.StoreStaffService;
import com.example.roadbikerental.service.support.BikeMaintenanceDiscountSupport;
import com.example.roadbikerental.vo.OrderVO;
import org.apache.commons.lang3.StringUtils;
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
 * 订单服务实现。
 */
@Service
public class RentalOrderServiceImpl extends ServiceImpl<RentalOrderMapper, RentalOrder> implements RentalOrderService {

    private final AppUserService appUserService;
    private final BikeInventoryService bikeInventoryService;
    private final BikePricingService bikePricingService;
    private final RentalStoreService rentalStoreService;
    private final StoreStaffService storeStaffService;
    private final BikeModelService bikeModelService;
    private final OrderNoGenerator orderNoGenerator;
    private final BikeMaintenanceDiscountSupport bikeMaintenanceDiscountSupport;

    public RentalOrderServiceImpl(AppUserService appUserService,
                                  BikeInventoryService bikeInventoryService,
                                  BikePricingService bikePricingService,
                                  RentalStoreService rentalStoreService,
                                  StoreStaffService storeStaffService,
                                  BikeModelService bikeModelService,
                                  OrderNoGenerator orderNoGenerator,
                                  BikeMaintenanceDiscountSupport bikeMaintenanceDiscountSupport) {
        this.appUserService = appUserService;
        this.bikeInventoryService = bikeInventoryService;
        this.bikePricingService = bikePricingService;
        this.rentalStoreService = rentalStoreService;
        this.storeStaffService = storeStaffService;
        this.bikeModelService = bikeModelService;
        this.orderNoGenerator = orderNoGenerator;
        this.bikeMaintenanceDiscountSupport = bikeMaintenanceDiscountSupport;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(Long userId, UserOrderCreateRequest request) {
        return createOrderInternal(userId, request.getBikeId(), request.getPickupStoreId(),
                request.getPlannedStartTime(), request.getPlannedEndTime(), request.getRemark(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createStoreOrder(Long staffId, Long storeId, StaffOfflineOrderCreateRequest request) {
        return createOrderInternal(request.getUserId(), request.getBikeId(), storeId,
                request.getPlannedStartTime(), request.getPlannedEndTime(), request.getRemark(), staffId);
    }

    @Override
    public PageResult<OrderVO> pageAdminOrders(Long current, Long size, String orderNo, Integer status, Long storeId) {
        LambdaQueryWrapper<RentalOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(orderNo), RentalOrder::getOrderNo, orderNo)
                .eq(status != null, RentalOrder::getOrderStatus, status)
                .eq(storeId != null, RentalOrder::getPickupStoreId, storeId)
                .orderByDesc(RentalOrder::getCreatedAt);
        Page<RentalOrder> page = page(new Page<>(current, size), wrapper);
        return convertOrderPage(page);
    }

    @Override
    public PageResult<OrderVO> pageUserOrders(Long current, Long size, Long userId, Integer status) {
        Page<RentalOrder> page = page(new Page<>(current, size), new LambdaQueryWrapper<RentalOrder>()
                .eq(RentalOrder::getUserId, userId)
                .eq(status != null, RentalOrder::getOrderStatus, status)
                .orderByDesc(RentalOrder::getCreatedAt));
        return convertOrderPage(page);
    }

    @Override
    public PageResult<OrderVO> pageStoreOrders(Long current, Long size, Long storeId, Integer status, String keyword) {
        LambdaQueryWrapper<RentalOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, RentalOrder::getOrderStatus, status)
                .and(query -> query.eq(RentalOrder::getPickupStoreId, storeId)
                        .or()
                        .eq(RentalOrder::getReturnStoreId, storeId)
                        .or()
                        .eq(RentalOrder::getOrderStatus, OrderStatusEnum.RENTING.getCode()))
                .orderByDesc(RentalOrder::getCreatedAt);

        if (StringUtils.isNotBlank(keyword)) {
            Set<Long> userIds = resolveUserIdsByKeyword(keyword);
            Set<Long> bikeIds = resolveBikeIdsByKeyword(keyword);
            wrapper.and(query -> {
                query.like(RentalOrder::getOrderNo, keyword);
                if (!userIds.isEmpty()) {
                    query.or().in(RentalOrder::getUserId, userIds);
                }
                if (!bikeIds.isEmpty()) {
                    query.or().in(RentalOrder::getBikeId, bikeIds);
                }
            });
        }

        Page<RentalOrder> page = page(new Page<>(current, size), wrapper);
        return convertOrderPage(page);
    }

    @Override
    public OrderVO getOrderDetail(Long orderId) {
        RentalOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        List<OrderVO> list = buildOrderVOs(Collections.singletonList(order));
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelUserOrder(Long userId, Long orderId, String reason) {
        RentalOrder order = getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (!Integer.valueOf(OrderStatusEnum.PENDING_PICKUP.getCode()).equals(order.getOrderStatus())
                && !Integer.valueOf(OrderStatusEnum.PENDING_PAYMENT.getCode()).equals(order.getOrderStatus())) {
            throw new BusinessException("当前订单状态不可取消");
        }

        order.setOrderStatus(OrderStatusEnum.CANCELED.getCode());
        order.setCancelReason(StringUtils.defaultIfBlank(reason, "用户主动取消"));
        order.setPaymentStatus(3);
        order.setDepositRefundStatus(2);
        updateById(order);

        BikeInventory bikeInventory = bikeInventoryService.getRequiredById(order.getBikeId());
        bikeInventory.setBikeStatus(BikeStatusEnum.AVAILABLE.getCode());
        bikeInventoryService.updateById(bikeInventory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminUpdateOrder(Long orderId, AdminOrderUpdateRequest request) {
        RentalOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (StringUtils.isNotBlank(request.getRemark())) {
            order.setRemark(request.getRemark());
        }
        if (request.getOrderStatus() != null) {
            if (!Integer.valueOf(OrderStatusEnum.CANCELED.getCode()).equals(request.getOrderStatus())) {
                throw new BusinessException("后台仅支持取消订单或修改备注");
            }
            if (Integer.valueOf(OrderStatusEnum.COMPLETED.getCode()).equals(order.getOrderStatus())) {
                throw new BusinessException("已完成订单不可取消");
            }
            order.setOrderStatus(OrderStatusEnum.CANCELED.getCode());
            order.setCancelReason(StringUtils.defaultIfBlank(request.getCancelReason(), "后台取消订单"));
            BikeInventory bikeInventory = bikeInventoryService.getRequiredById(order.getBikeId());
            bikeInventory.setBikeStatus(BikeStatusEnum.AVAILABLE.getCode());
            bikeInventoryService.updateById(bikeInventory);
        }
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO pickupBike(Long staffId, Long storeId, StaffPickupRequest request) {
        RentalOrder order = getById(request.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!Integer.valueOf(OrderStatusEnum.PENDING_PICKUP.getCode()).equals(order.getOrderStatus())) {
            throw new BusinessException("当前订单不可办理取车");
        }
        if (!storeId.equals(order.getPickupStoreId())) {
            throw new BusinessException("只能办理本门店的取车业务");
        }

        order.setActualPickupTime(LocalDateTime.now());
        order.setCreatedStaffId(staffId);
        order.setOrderStatus(OrderStatusEnum.RENTING.getCode());
        updateById(order);

        BikeInventory bikeInventory = bikeInventoryService.getRequiredById(order.getBikeId());
        bikeInventory.setBikeStatus(BikeStatusEnum.RENTING.getCode());
        bikeInventoryService.updateById(bikeInventory);
        return getOrderDetail(order.getOrderId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO returnBike(Long staffId, Long storeId, StaffReturnRequest request) {
        RentalOrder order = getById(request.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!Integer.valueOf(OrderStatusEnum.RENTING.getCode()).equals(order.getOrderStatus())) {
            throw new BusinessException("当前订单不可办理还车");
        }

        order.setActualReturnTime(LocalDateTime.now());
        order.setReturnStoreId(storeId);
        order.setOrderStatus(OrderStatusEnum.PENDING_SETTLEMENT.getCode());
        if (order.getCreatedStaffId() == null) {
            order.setCreatedStaffId(staffId);
        }
        updateById(order);

        BikeInventory bikeInventory = bikeInventoryService.getRequiredById(order.getBikeId());
        bikeInventory.setCurrentStoreId(storeId);
        bikeInventory.setBikeStatus(BikeStatusEnum.RESERVED.getCode());
        bikeInventoryService.updateById(bikeInventory);
        return getOrderDetail(order.getOrderId());
    }

    private OrderVO createOrderInternal(Long userId,
                                        Long bikeId,
                                        Long pickupStoreId,
                                        LocalDateTime plannedStartTime,
                                        LocalDateTime plannedEndTime,
                                        String remark,
                                        Long createdStaffId) {
        appUserService.getCurrentUser(userId);
        if (!plannedEndTime.isAfter(plannedStartTime)) {
            throw new BusinessException("计划还车时间必须晚于计划取车时间");
        }

        BikeInventory bikeInventory = bikeInventoryService.getRequiredById(bikeId);
        if (!Integer.valueOf(BikeStatusEnum.AVAILABLE.getCode()).equals(bikeInventory.getBikeStatus())) {
            throw new BusinessException("当前车辆不可预约");
        }
        if (!pickupStoreId.equals(bikeInventory.getCurrentStoreId())) {
            throw new BusinessException("取车门店与车辆所在门店不一致");
        }

        BikePricing pricing = bikePricingService.getCurrentPricingByModelId(bikeInventory.getModelId());
        if (pricing == null) {
            throw new BusinessException("该车辆暂无有效价格策略");
        }

        long minutes = ChronoUnit.MINUTES.between(plannedStartTime, plannedEndTime);
        int rentDays = (int) Math.max(1, Math.ceil(minutes / 1440D));
        BigDecimal effectiveDailyRent = bikeMaintenanceDiscountSupport.applyDiscount(
                pricing.getDailyRent(),
                bikeMaintenanceDiscountSupport.countCompletedHistory(bikeId));
        BigDecimal rentAmount = effectiveDailyRent.multiply(BigDecimal.valueOf(rentDays));
        BigDecimal totalAmount = rentAmount.add(pricing.getDepositAmount());

        RentalOrder order = new RentalOrder();
        order.setOrderNo(orderNoGenerator.generate());
        order.setUserId(userId);
        order.setBikeId(bikeId);
        order.setPricingId(pricing.getPricingId());
        order.setPickupStoreId(pickupStoreId);
        order.setPlannedStartTime(plannedStartTime);
        order.setPlannedEndTime(plannedEndTime);
        order.setRentDays(rentDays);
        order.setDailyRentAmount(effectiveDailyRent);
        order.setRentAmount(rentAmount);
        order.setDepositAmount(pricing.getDepositAmount());
        order.setOvertimeAmount(BigDecimal.ZERO);
        order.setDamageAmount(BigDecimal.ZERO);
        order.setOtherAmount(BigDecimal.ZERO);
        order.setTotalAmount(totalAmount);
        order.setOrderStatus(OrderStatusEnum.PENDING_PICKUP.getCode());
        order.setPaymentStatus(1);
        order.setDepositRefundStatus(0);
        order.setCreatedStaffId(createdStaffId);
        order.setRemark(remark);
        save(order);

        bikeInventory.setBikeStatus(BikeStatusEnum.RESERVED.getCode());
        bikeInventoryService.updateById(bikeInventory);
        return getOrderDetail(order.getOrderId());
    }

    private PageResult<OrderVO> convertOrderPage(Page<RentalOrder> page) {
        List<OrderVO> records = buildOrderVOs(page.getRecords());
        Page<OrderVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(records);
        return PageResult.of(resultPage);
    }

    private Set<Long> resolveUserIdsByKeyword(String keyword) {
        return appUserService.list(new LambdaQueryWrapper<AppUser>()
                        .select(AppUser::getUserId)
                        .and(query -> query.like(AppUser::getUsername, keyword)
                                .or()
                                .like(AppUser::getNickname, keyword)))
                .stream()
                .map(AppUser::getUserId)
                .collect(Collectors.toSet());
    }

    private Set<Long> resolveBikeIdsByKeyword(String keyword) {
        Set<Long> matchedModelIds = bikeModelService.list(new LambdaQueryWrapper<BikeModel>()
                        .select(BikeModel::getModelId)
                        .and(query -> query.like(BikeModel::getBrandName, keyword)
                                .or()
                                .like(BikeModel::getModelName, keyword)))
                .stream()
                .map(BikeModel::getModelId)
                .collect(Collectors.toSet());

        LambdaQueryWrapper<BikeInventory> wrapper = new LambdaQueryWrapper<BikeInventory>()
                .select(BikeInventory::getBikeId)
                .like(BikeInventory::getBikeCode, keyword);
        if (!matchedModelIds.isEmpty()) {
            wrapper.or().in(BikeInventory::getModelId, matchedModelIds);
        }

        return bikeInventoryService.list(wrapper).stream()
                .map(BikeInventory::getBikeId)
                .collect(Collectors.toSet());
    }

    private List<OrderVO> buildOrderVOs(List<RentalOrder> orders) {
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> userIds = orders.stream().map(RentalOrder::getUserId).collect(Collectors.toSet());
        Set<Long> bikeIds = orders.stream().map(RentalOrder::getBikeId).collect(Collectors.toSet());
        Set<Long> pricingIds = orders.stream().map(RentalOrder::getPricingId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Set<Long> pickupStoreIds = orders.stream().map(RentalOrder::getPickupStoreId).collect(Collectors.toSet());
        Set<Long> returnStoreIds = orders.stream().map(RentalOrder::getReturnStoreId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Set<Long> staffIds = orders.stream().map(RentalOrder::getCreatedStaffId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Long, AppUser> userMap = appUserService.listByIds(userIds).stream()
                .collect(Collectors.toMap(AppUser::getUserId, Function.identity()));
        Map<Long, BikeInventory> bikeMap = bikeInventoryService.listByIds(bikeIds).stream()
                .collect(Collectors.toMap(BikeInventory::getBikeId, Function.identity()));
        Map<Long, BikePricing> pricingMap = pricingIds.isEmpty()
                ? Collections.emptyMap()
                : bikePricingService.listByIds(pricingIds).stream()
                .collect(Collectors.toMap(BikePricing::getPricingId, Function.identity()));
        Set<Long> modelIds = bikeMap.values().stream().map(BikeInventory::getModelId).collect(Collectors.toSet());
        Map<Long, BikeModel> modelMap = bikeModelService.listByIds(modelIds).stream()
                .collect(Collectors.toMap(BikeModel::getModelId, Function.identity()));
        Map<Long, RentalStore> pickupStoreMap = rentalStoreService.listByIds(pickupStoreIds).stream()
                .collect(Collectors.toMap(RentalStore::getStoreId, Function.identity()));
        Map<Long, RentalStore> returnStoreMap = returnStoreIds.isEmpty()
                ? Collections.emptyMap()
                : rentalStoreService.listByIds(returnStoreIds).stream()
                .collect(Collectors.toMap(RentalStore::getStoreId, Function.identity()));
        Map<Long, StoreStaff> staffMap = staffIds.isEmpty()
                ? Collections.emptyMap()
                : storeStaffService.listByIds(staffIds).stream()
                .collect(Collectors.toMap(StoreStaff::getStaffId, Function.identity()));

        return orders.stream().map(order -> {
            AppUser user = userMap.get(order.getUserId());
            BikeInventory bike = bikeMap.get(order.getBikeId());
            BikePricing pricing = order.getPricingId() == null ? null : pricingMap.get(order.getPricingId());
            BikeModel model = bike == null ? null : modelMap.get(bike.getModelId());
            RentalStore pickupStore = pickupStoreMap.get(order.getPickupStoreId());
            RentalStore returnStore = order.getReturnStoreId() == null ? null : returnStoreMap.get(order.getReturnStoreId());
            StoreStaff staff = order.getCreatedStaffId() == null ? null : staffMap.get(order.getCreatedStaffId());

            OrderVO vo = new OrderVO();
            vo.setOrderId(order.getOrderId());
            vo.setOrderNo(order.getOrderNo());
            vo.setUserId(order.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
            }
            vo.setBikeId(order.getBikeId());
            if (bike != null) {
                vo.setBikeCode(bike.getBikeCode());
                vo.setModelId(bike.getModelId());
            }
            if (model != null) {
                vo.setBrandName(model.getBrandName());
                vo.setModelName(model.getModelName());
            }
            vo.setPickupStoreId(order.getPickupStoreId());
            if (pickupStore != null) {
                vo.setPickupStoreName(pickupStore.getStoreName());
            }
            vo.setReturnStoreId(order.getReturnStoreId());
            if (returnStore != null) {
                vo.setReturnStoreName(returnStore.getStoreName());
            }
            vo.setCreatedStaffId(order.getCreatedStaffId());
            if (staff != null) {
                vo.setCreatedStaffName(staff.getStaffName());
            }
            vo.setPlannedStartTime(order.getPlannedStartTime());
            vo.setPlannedEndTime(order.getPlannedEndTime());
            vo.setActualPickupTime(order.getActualPickupTime());
            vo.setActualReturnTime(order.getActualReturnTime());
            vo.setRentDays(order.getRentDays());
            vo.setDailyRentAmount(order.getDailyRentAmount());
            vo.setRentAmount(order.getRentAmount());
            vo.setDepositAmount(order.getDepositAmount());
            if (pricing != null) {
                vo.setOvertimeFeePerHour(pricing.getOvertimeFeePerHour());
            }
            vo.setOvertimeAmount(order.getOvertimeAmount());
            vo.setDamageAmount(order.getDamageAmount());
            vo.setOtherAmount(order.getOtherAmount());
            vo.setTotalAmount(order.getTotalAmount());
            vo.setOrderStatus(order.getOrderStatus());
            vo.setPaymentStatus(order.getPaymentStatus());
            vo.setDepositRefundStatus(order.getDepositRefundStatus());
            vo.setCancelReason(order.getCancelReason());
            vo.setRemark(order.getRemark());
            vo.setCreatedAt(order.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
    }
}
