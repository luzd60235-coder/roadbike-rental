package com.example.roadbikerental.service;

import com.example.roadbikerental.vo.admin.AdminStatisticsVO;

/**
 * 后台统计服务。
 */
public interface StatisticsService {

    AdminStatisticsVO getOverview();
}
