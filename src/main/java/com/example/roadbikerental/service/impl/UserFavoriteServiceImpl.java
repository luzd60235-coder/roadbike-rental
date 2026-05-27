package com.example.roadbikerental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.roadbikerental.common.exception.BusinessException;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.entity.BikeModel;
import com.example.roadbikerental.entity.UserFavorite;
import com.example.roadbikerental.mapper.UserFavoriteMapper;
import com.example.roadbikerental.service.BikeModelService;
import com.example.roadbikerental.service.UserFavoriteService;
import com.example.roadbikerental.vo.user.FavoriteVO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 收藏服务实现。
 */
@Service
public class UserFavoriteServiceImpl extends ServiceImpl<UserFavoriteMapper, UserFavorite> implements UserFavoriteService {

    private final BikeModelService bikeModelService;

    public UserFavoriteServiceImpl(BikeModelService bikeModelService) {
        this.bikeModelService = bikeModelService;
    }

    @Override
    public void addFavorite(Long userId, Long modelId) {
        if (count(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getModelId, modelId)) > 0) {
            throw new BusinessException("该车辆已收藏");
        }
        if (bikeModelService.getById(modelId) == null) {
            throw new BusinessException("车辆配置不存在");
        }
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setModelId(modelId);
        save(favorite);
    }

    @Override
    public void removeFavorite(Long userId, Long modelId) {
        remove(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getModelId, modelId));
    }

    @Override
    public PageResult<FavoriteVO> pageFavorites(Long current, Long size, Long userId) {
        Page<UserFavorite> page = page(new Page<>(current, size), new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .orderByDesc(UserFavorite::getCreatedAt));
        List<FavoriteVO> records = buildVOs(page.getRecords());
        Page<FavoriteVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(records);
        return PageResult.of(resultPage);
    }

    private List<FavoriteVO> buildVOs(List<UserFavorite> favorites) {
        if (favorites.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> modelIds = favorites.stream().map(UserFavorite::getModelId).collect(Collectors.toSet());
        Map<Long, BikeModel> modelMap = bikeModelService.listByIds(modelIds).stream()
                .collect(Collectors.toMap(BikeModel::getModelId, Function.identity()));
        return favorites.stream().map(favorite -> {
            FavoriteVO vo = new FavoriteVO();
            vo.setFavoriteId(favorite.getFavoriteId());
            vo.setModelId(favorite.getModelId());
            vo.setCreatedAt(favorite.getCreatedAt());
            BikeModel bikeModel = modelMap.get(favorite.getModelId());
            if (bikeModel != null) {
                vo.setBrandName(bikeModel.getBrandName());
                vo.setModelName(bikeModel.getModelName());
                vo.setBikeType(bikeModel.getBikeType());
                vo.setMarketPrice(bikeModel.getMarketPrice());
                vo.setCoverImageUrl(bikeModel.getCoverImageUrl());
            }
            return vo;
        }).collect(Collectors.toList());
    }
}
