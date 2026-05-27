package com.example.roadbikerental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.roadbikerental.common.auth.LoginUser;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.exception.BusinessException;
import com.example.roadbikerental.common.util.JwtUtil;
import com.example.roadbikerental.common.util.PasswordUtil;
import com.example.roadbikerental.dto.LoginRequest;
import com.example.roadbikerental.dto.user.UserProfileUpdateRequest;
import com.example.roadbikerental.dto.user.UserRegisterRequest;
import com.example.roadbikerental.entity.AppUser;
import com.example.roadbikerental.mapper.AppUserMapper;
import com.example.roadbikerental.service.AppUserService;
import com.example.roadbikerental.vo.LoginVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现。
 */
@Service
public class AppUserServiceImpl extends ServiceImpl<AppUserMapper, AppUser> implements AppUserService {

    private final JwtUtil jwtUtil;

    public AppUserServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(UserRegisterRequest request) {
        if (!StringUtils.equals(request.getPassword(), request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        if (count(new LambdaQueryWrapper<AppUser>().eq(AppUser::getUsername, request.getUsername())) > 0) {
            throw new BusinessException("用户名已存在");
        }
        if (count(new LambdaQueryWrapper<AppUser>().eq(AppUser::getPhone, request.getPhone())) > 0) {
            throw new BusinessException("手机号已注册");
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(StringUtils.trim(request.getUsername()));
        appUser.setPasswordHash(PasswordUtil.encode(request.getPassword()));
        appUser.setNickname(StringUtils.trim(request.getNickname()));
        appUser.setPhone(StringUtils.trim(request.getPhone()));
        appUser.setEmail(StringUtils.trimToNull(request.getEmail()));
        appUser.setGender(request.getGender());
        appUser.setIdCardNo(StringUtils.trim(request.getIdCardNo()));
        appUser.setMemberLevel("normal");
        appUser.setStatus(1);
        appUser.setLastLoginAt(LocalDateTime.now());
        save(appUser);
        return buildLoginVO(appUser);
    }

    @Override
    public LoginVO login(LoginRequest request) {
        AppUser appUser = getOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getUsername, request.getUsername())
                .last("limit 1"));
        if (appUser == null) {
            throw new BusinessException("用户账号不存在");
        }
        if (!Integer.valueOf(1).equals(appUser.getStatus())) {
            throw new BusinessException("用户账号已停用");
        }
        if (!PasswordUtil.matches(request.getPassword(), appUser.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }
        appUser.setLastLoginAt(LocalDateTime.now());
        updateById(appUser);
        return buildLoginVO(appUser);
    }

    @Override
    public AppUser getCurrentUser(Long userId) {
        AppUser appUser = getById(userId);
        if (appUser == null || !Integer.valueOf(1).equals(appUser.getStatus())) {
            throw new BusinessException("用户不存在或已停用");
        }
        return appUser;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppUser updateProfile(Long userId, UserProfileUpdateRequest request) {
        AppUser appUser = getCurrentUser(userId);
        if (StringUtils.isNotBlank(request.getPhone()) && !StringUtils.equals(request.getPhone(), appUser.getPhone())) {
            if (count(new LambdaQueryWrapper<AppUser>()
                    .eq(AppUser::getPhone, request.getPhone())
                    .ne(AppUser::getUserId, userId)) > 0) {
                throw new BusinessException("手机号已被其他账号使用");
            }
        }

        String password = StringUtils.trimToNull(request.getPassword());
        String confirmPassword = StringUtils.trimToNull(request.getConfirmPassword());
        if (StringUtils.isNotBlank(password) || StringUtils.isNotBlank(confirmPassword)) {
            if (!StringUtils.equals(password, confirmPassword)) {
                throw new BusinessException("两次输入的新密码不一致");
            }
            appUser.setPasswordHash(PasswordUtil.encode(password));
        }

        appUser.setNickname(StringUtils.trimToNull(request.getNickname()));
        appUser.setGender(request.getGender());
        appUser.setPhone(StringUtils.trimToNull(request.getPhone()));
        appUser.setEmail(StringUtils.trimToNull(request.getEmail()));
        appUser.setIdCardNo(StringUtils.trimToNull(request.getIdCardNo()));
        updateById(appUser);
        return appUser;
    }

    private LoginVO buildLoginVO(AppUser appUser) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(appUser.getUserId());
        loginUser.setUsername(appUser.getUsername());
        loginUser.setDisplayName(StringUtils.defaultIfBlank(appUser.getNickname(), appUser.getUsername()));
        loginUser.setRoleType(RoleType.USER);

        LoginVO loginVO = new LoginVO();
        loginVO.setUserId(appUser.getUserId());
        loginVO.setUsername(appUser.getUsername());
        loginVO.setDisplayName(StringUtils.defaultIfBlank(appUser.getNickname(), appUser.getUsername()));
        loginVO.setRoleType(RoleType.USER);
        loginVO.setToken(jwtUtil.generateToken(loginUser));
        return loginVO;
    }
}
