package com.example.roadbikerental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.roadbikerental.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏 Mapper。
 */
@Mapper
public interface UserFavoriteMapper extends BaseMapper<UserFavorite> {
}
