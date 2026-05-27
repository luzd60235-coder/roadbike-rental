package com.example.roadbikerental.controller.staff;

import com.example.roadbikerental.common.annotation.RoleRequired;
import com.example.roadbikerental.common.auth.AuthContext;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.result.ApiResponse;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.staff.StaffSettlementCreateRequest;
import com.example.roadbikerental.service.RentalOrderSettlementService;
import com.example.roadbikerental.vo.staff.SettlementVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门店结算控制器。
 */
@RestController
@RequestMapping("/api/staff/settlements")
@RoleRequired(RoleType.STAFF)
public class StaffSettlementController {

    private final RentalOrderSettlementService rentalOrderSettlementService;

    public StaffSettlementController(RentalOrderSettlementService rentalOrderSettlementService) {
        this.rentalOrderSettlementService = rentalOrderSettlementService;
    }

    /**
     * 员工办理租赁结算。
     */
    @PostMapping
    public ApiResponse<SettlementVO> create(@Validated @RequestBody StaffSettlementCreateRequest request) {
        return ApiResponse.success(rentalOrderSettlementService.createSettlement(
                AuthContext.getUserId(), AuthContext.getStoreId(), request));
    }

    /**
     * 查询当前门店结算记录。
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<SettlementVO>> page(@RequestParam(defaultValue = "1") Long current,
                                                      @RequestParam(defaultValue = "10") Long size) {
        return ApiResponse.success(rentalOrderSettlementService.pageStoreSettlements(current, size, AuthContext.getStoreId()));
    }
}
