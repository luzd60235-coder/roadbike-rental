package com.example.roadbikerental.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.admin.AdminBikeModelSaveRequest;
import com.example.roadbikerental.entity.BikeModel;

/**
 * 车辆配置服务。
 */
public interface BikeModelService extends IService<BikeModel> {

    PageResult<BikeModel> pageModels(Long current, Long size, String keyword, Integer status);

    BikeModel saveOrUpdateModel(AdminBikeModelSaveRequest request);
}
