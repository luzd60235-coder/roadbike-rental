package com.example.roadbikerental.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.staff.StaffSettlementCreateRequest;
import com.example.roadbikerental.entity.RentalOrderSettlement;
import com.example.roadbikerental.vo.staff.SettlementVO;

/**
 * 订单结算服务。
 */
public interface RentalOrderSettlementService extends IService<RentalOrderSettlement> {

    SettlementVO createSettlement(Long staffId, Long storeId, StaffSettlementCreateRequest request);

    PageResult<SettlementVO> pageStoreSettlements(Long current, Long size, Long storeId);
}
