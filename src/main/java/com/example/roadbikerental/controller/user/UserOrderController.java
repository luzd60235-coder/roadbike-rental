package com.example.roadbikerental.controller.user;

import com.example.roadbikerental.common.annotation.RoleRequired;
import com.example.roadbikerental.common.auth.AuthContext;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.exception.BusinessException;
import com.example.roadbikerental.common.result.ApiResponse;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.user.UserOrderCreateRequest;
import com.example.roadbikerental.service.RentalOrderService;
import com.example.roadbikerental.vo.OrderVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户订单控制器。
 */
@RestController
@RequestMapping("/api/user/orders")
@RoleRequired(RoleType.USER)
public class UserOrderController {

    private final RentalOrderService rentalOrderService;

    public UserOrderController(RentalOrderService rentalOrderService) {
        this.rentalOrderService = rentalOrderService;
    }

    /**
     * 用户创建租赁订单。
     */
    @PostMapping
    public ApiResponse<OrderVO> create(@Validated @RequestBody UserOrderCreateRequest request) {
        return ApiResponse.success(rentalOrderService.createOrder(AuthContext.getUserId(), request));
    }

    /**
     * 用户分页查询自己的订单。
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<OrderVO>> page(@RequestParam(defaultValue = "1") Long current,
                                                 @RequestParam(defaultValue = "10") Long size,
                                                 @RequestParam(required = false) Integer status) {
        return ApiResponse.success(rentalOrderService.pageUserOrders(current, size, AuthContext.getUserId(), status));
    }

    /**
     * 查看订单详情。
     */
    @GetMapping("/{orderId}")
    public ApiResponse<OrderVO> detail(@PathVariable Long orderId) {
        OrderVO orderVO = rentalOrderService.getOrderDetail(orderId);
        if (!AuthContext.getUserId().equals(orderVO.getUserId())) {
            throw new BusinessException("无权查看该订单");
        }
        return ApiResponse.success(orderVO);
    }

    /**
     * 用户取消订单。
     */
    @PutMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long orderId,
                                    @RequestParam(required = false) String reason) {
        rentalOrderService.cancelUserOrder(AuthContext.getUserId(), orderId, reason);
        return ApiResponse.success("取消成功", null);
    }
}
