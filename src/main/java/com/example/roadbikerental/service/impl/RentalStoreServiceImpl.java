package com.example.roadbikerental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.roadbikerental.common.exception.BusinessException;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.admin.AdminStoreSaveRequest;
import com.example.roadbikerental.entity.RentalStore;
import com.example.roadbikerental.mapper.RentalStoreMapper;
import com.example.roadbikerental.service.RentalStoreService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 门店服务实现。
 */
@Service
public class RentalStoreServiceImpl extends ServiceImpl<RentalStoreMapper, RentalStore> implements RentalStoreService {

    @Override
    public PageResult<RentalStore> pageStores(Long current, Long size, String city, Integer status) {
        Page<RentalStore> page = page(new Page<>(current, size), new LambdaQueryWrapper<RentalStore>()
                .like(StringUtils.isNotBlank(city), RentalStore::getCity, city)
                .eq(status != null, RentalStore::getStatus, status)
                .orderByDesc(RentalStore::getCreatedAt));
        return PageResult.of(page);
    }

    @Override
    public RentalStore saveOrUpdateStore(AdminStoreSaveRequest request) {
        RentalStore store = request.getStoreId() == null ? new RentalStore() : getById(request.getStoreId());
        if (request.getStoreId() != null && store == null) {
            throw new BusinessException("门店不存在");
        }
        BeanUtils.copyProperties(request, store);
        saveOrUpdate(store);
        return store;
    }
}
