package com.example.roadbikerental.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.entity.UserFavorite;
import com.example.roadbikerental.vo.user.FavoriteVO;

/**
 * 收藏服务。
 */
public interface UserFavoriteService extends IService<UserFavorite> {

    void addFavorite(Long userId, Long modelId);

    void removeFavorite(Long userId, Long modelId);

    PageResult<FavoriteVO> pageFavorites(Long current, Long size, Long userId);
}
