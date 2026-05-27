package com.example.roadbikerental.mapper;

import com.example.roadbikerental.entity.FinancialOverview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 财务概览 Mapper。
 */
@Mapper
public interface FinancialOverviewMapper {

    @Select("select total_orders, total_rent_income, total_deposit_collected, total_deposit_refunded, total_extra_income, total_maintenance_cost, estimated_net_income from vw_financial_overview")
    FinancialOverview selectOverview();
}
