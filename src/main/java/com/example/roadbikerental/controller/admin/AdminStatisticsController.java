package com.example.roadbikerental.controller.admin;

import com.example.roadbikerental.common.annotation.RoleRequired;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.result.ApiResponse;
import com.example.roadbikerental.service.StatisticsService;
import com.example.roadbikerental.vo.admin.AdminStatisticsVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台统计控制器。
 */
@RestController
@RequestMapping("/api/admin/statistics")
@RoleRequired(RoleType.ADMIN)
public class AdminStatisticsController {

    private final StatisticsService statisticsService;

    public AdminStatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * 获取后台概览统计数据。
     */
    @GetMapping("/overview")
    public ApiResponse<AdminStatisticsVO> overview() {
        return ApiResponse.success(statisticsService.getOverview());
    }
}
