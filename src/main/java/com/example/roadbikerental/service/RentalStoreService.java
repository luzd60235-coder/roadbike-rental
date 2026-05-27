package com.example.roadbikerental.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.admin.AdminStoreSaveRequest;
import com.example.roadbikerental.entity.RentalStore;

/**
 * 门店服务。
 */
public interface RentalStoreService extends IService<RentalStore> {

    PageResult<RentalStore> pageStores(Long current, Long size, String city, Integer status);

    RentalStore saveOrUpdateStore(AdminStoreSaveRequest request);
}
