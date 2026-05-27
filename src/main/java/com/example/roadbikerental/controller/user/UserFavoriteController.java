package com.example.roadbikerental.controller.user;

import com.example.roadbikerental.common.annotation.RoleRequired;
import com.example.roadbikerental.common.auth.AuthContext;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.result.ApiResponse;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.service.UserFavoriteService;
import com.example.roadbikerental.vo.user.FavoriteVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户收藏控制器。
 */
@RestController
@RequestMapping("/api/user/favorites")
@RoleRequired(RoleType.USER)
public class UserFavoriteController {

    private final UserFavoriteService userFavoriteService;

    public UserFavoriteController(UserFavoriteService userFavoriteService) {
        this.userFavoriteService = userFavoriteService;
    }

    /**
     * 添加收藏。
     */
    @PostMapping
    public ApiResponse<Void> add(@RequestParam Long modelId) {
        userFavoriteService.addFavorite(AuthContext.getUserId(), modelId);
        return ApiResponse.success("收藏成功", null);
    }

    /**
     * 取消收藏。
     */
    @DeleteMapping
    public ApiResponse<Void> remove(@RequestParam Long modelId) {
        userFavoriteService.removeFavorite(AuthContext.getUserId(), modelId);
        return ApiResponse.success("取消成功", null);
    }

    /**
     * 分页查询用户收藏。
     */
    @GetMapping("/page")
    public ApiResponse<PageResult<FavoriteVO>> page(@RequestParam(defaultValue = "1") Long current,
                                                    @RequestParam(defaultValue = "10") Long size) {
        return ApiResponse.success(userFavoriteService.pageFavorites(current, size, AuthContext.getUserId()));
    }
}
