package com.example.roadbikerental.vo.user;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户收藏响应。
 */
@Data
public class FavoriteVO {

    private Long favoriteId;

    private Long modelId;

    private String brandName;

    private String modelName;

    private String bikeType;

    private BigDecimal marketPrice;

    private String coverImageUrl;

    private LocalDateTime createdAt;
}
