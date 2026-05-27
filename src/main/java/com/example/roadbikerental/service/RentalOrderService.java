package com.example.roadbikerental.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.admin.AdminOrderUpdateRequest;
import com.example.roadbikerental.dto.staff.StaffOfflineOrderCreateRequest;
import com.example.roadbikerental.dto.staff.StaffPickupRequest;
import com.example.roadbikerental.dto.staff.StaffReturnRequest;
import com.example.roadbikerental.dto.user.UserOrderCreateRequest;
import com.example.roadbikerental.entity.RentalOrder;
import com.example.roadbikerental.vo.OrderVO;

/**
 * 订单服务。
 */
public interface RentalOrderService extends IService<RentalOrder> {

    OrderVO createOrder(Long userId, UserOrderCreateRequest request);

    OrderVO createStoreOrder(Long staffId, Long storeId, StaffOfflineOrderCreateRequest request);

    PageResult<OrderVO> pageAdminOrders(Long current, Long size, String orderNo, Integer status, Long storeId);

    PageResult<OrderVO> pageUserOrders(Long current, Long size, Long userId, Integer status);

    PageResult<OrderVO> pageStoreOrders(Long current, Long size, Long storeId, Integer status, String keyword);

    OrderVO getOrderDetail(Long orderId);

    void cancelUserOrder(Long userId, Long orderId, String reason);

    void adminUpdateOrder(Long orderId, AdminOrderUpdateRequest request);

    OrderVO pickupBike(Long staffId, Long storeId, StaffPickupRequest request);

    OrderVO returnBike(Long staffId, Long storeId, StaffReturnRequest request);
}
