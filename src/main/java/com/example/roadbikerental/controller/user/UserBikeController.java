package com.example.roadbikerental.controller.user;

import com.example.roadbikerental.common.annotation.RoleRequired;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.result.ApiResponse;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.service.BikeInventoryService;
import com.example.roadbikerental.vo.user.UserBikeDetailVO;
import com.example.roadbikerental.vo.user.UserBikeVO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户车辆查询控制器。
 */
@RestController
@RequestMapping("/api/user/bikes")
@RoleRequired(RoleType.USER)
public class UserBikeController {

    private final BikeInventoryService bikeInventoryService;

    public UserBikeController(BikeInventoryService bikeInventoryService) {
        this.bikeInventoryService = bikeInventoryService;
    }

    /**
     * 分页查询可租赁车辆。
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<UserBikeVO>> page(@RequestParam(defaultValue = "1") Long current,
                                                    @RequestParam(defaultValue = "10") Long size,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) Long storeId,
                                                    @RequestParam(required = false) Long modelId,
                                                    @RequestParam(required = false) Integer status) {
        return ApiResponse.success(bikeInventoryService.pageBikes(current, size, keyword, storeId, modelId, status));
    }

    @GetMapping("/{bikeId}")
    public ApiResponse<UserBikeDetailVO> detail(@PathVariable Long bikeId) {
        return ApiResponse.success(bikeInventoryService.getUserBikeDetail(bikeId));
    }
}
