package com.example.roadbikerental.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.admin.AdminBikeInventorySaveRequest;
import com.example.roadbikerental.entity.BikeInventory;
import com.example.roadbikerental.vo.user.UserBikeDetailVO;
import com.example.roadbikerental.vo.user.UserBikeVO;

/**
 * 车辆库存服务。
 */
public interface BikeInventoryService extends IService<BikeInventory> {

    PageResult<BikeInventory> pageAdminInventory(Long current, Long size, String keyword, Long storeId, Long modelId, Integer status);

    PageResult<UserBikeVO> pageBikes(Long current, Long size, String keyword, Long storeId, Long modelId, Integer status);

    UserBikeDetailVO getUserBikeDetail(Long bikeId);

    BikeInventory saveOrUpdateInventory(AdminBikeInventorySaveRequest request);

    BikeInventory getRequiredById(Long bikeId);
}
