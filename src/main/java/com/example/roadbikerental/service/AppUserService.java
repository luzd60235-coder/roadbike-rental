package com.example.roadbikerental.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.roadbikerental.dto.LoginRequest;
import com.example.roadbikerental.dto.user.UserProfileUpdateRequest;
import com.example.roadbikerental.dto.user.UserRegisterRequest;
import com.example.roadbikerental.entity.AppUser;
import com.example.roadbikerental.vo.LoginVO;

/**
 * 用户服务。
 */
public interface AppUserService extends IService<AppUser> {

    LoginVO register(UserRegisterRequest request);

    LoginVO login(LoginRequest request);

    AppUser getCurrentUser(Long userId);

    AppUser updateProfile(Long userId, UserProfileUpdateRequest request);
}
