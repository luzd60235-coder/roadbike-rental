package com.example.roadbikerental.controller.admin;

import com.example.roadbikerental.common.annotation.RoleRequired;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.result.ApiResponse;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.admin.AdminOrderUpdateRequest;
import com.example.roadbikerental.service.RentalOrderService;
import com.example.roadbikerental.vo.OrderVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台订单管理控制器。
 */
@RestController
@RequestMapping("/api/admin/orders")
@RoleRequired(RoleType.ADMIN)
public class AdminOrderController {

    private final RentalOrderService rentalOrderService;

    public AdminOrderController(RentalOrderService rentalOrderService) {
        this.rentalOrderService = rentalOrderService;
    }

    /**
     * 订单分页查询。
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<OrderVO>> page(@RequestParam(defaultValue = "1") Long current,
                                                 @RequestParam(defaultValue = "10") Long size,
                                                 @RequestParam(required = false) String orderNo,
                                                 @RequestParam(required = false) Integer status,
                                                 @RequestParam(required = false) Long storeId) {
        return ApiResponse.success(rentalOrderService.pageAdminOrders(current, size, orderNo, status, storeId));
    }

    /**
     * 订单详情。
     */
    @GetMapping("/{orderId}")
    public ApiResponse<OrderVO> detail(@PathVariable Long orderId) {
        return ApiResponse.success(rentalOrderService.getOrderDetail(orderId));
    }

    /**
     * 后台更新订单备注或取消订单。
     */
    @PutMapping("/{orderId}")
    public ApiResponse<Void> update(@PathVariable Long orderId,
                                    @Validated @RequestBody AdminOrderUpdateRequest request) {
        rentalOrderService.adminUpdateOrder(orderId, request);
        return ApiResponse.success("处理成功", null);
    }
}
