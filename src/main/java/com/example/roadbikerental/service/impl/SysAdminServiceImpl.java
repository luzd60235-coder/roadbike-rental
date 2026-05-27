package com.example.roadbikerental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.roadbikerental.common.auth.LoginUser;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.exception.BusinessException;
import com.example.roadbikerental.common.util.JwtUtil;
import com.example.roadbikerental.common.util.PasswordUtil;
import com.example.roadbikerental.dto.LoginRequest;
import com.example.roadbikerental.dto.admin.AdminProfileUpdateRequest;
import com.example.roadbikerental.dto.admin.AdminRegisterRequest;
import com.example.roadbikerental.entity.SysAdmin;
import com.example.roadbikerental.mapper.SysAdminMapper;
import com.example.roadbikerental.service.SysAdminService;
import com.example.roadbikerental.vo.LoginVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 后台管理员服务实现。
 */
@Service
public class SysAdminServiceImpl extends ServiceImpl<SysAdminMapper, SysAdmin> implements SysAdminService {

    private final JwtUtil jwtUtil;

    public SysAdminServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(AdminRegisterRequest request) {
        if (!StringUtils.equals(request.getPassword(), request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        if (count(new LambdaQueryWrapper<SysAdmin>().eq(SysAdmin::getUsername, request.getUsername())) > 0) {
            throw new BusinessException("用户名已存在");
        }
        if (StringUtils.isNotBlank(request.getPhone())
                && count(new LambdaQueryWrapper<SysAdmin>().eq(SysAdmin::getPhone, request.getPhone())) > 0) {
            throw new BusinessException("手机号已注册");
        }

        SysAdmin admin = new SysAdmin();
        admin.setUsername(StringUtils.trim(request.getUsername()));
        admin.setPasswordHash(PasswordUtil.encode(request.getPassword()));
        admin.setRealName(StringUtils.trim(request.getRealName()));
        admin.setPhone(StringUtils.trimToNull(request.getPhone()));
        admin.setEmail(StringUtils.trimToNull(request.getEmail()));
        admin.setIdCardNo(StringUtils.trim(request.getIdCardNo()));
        admin.setStatus(1);
        admin.setLastLoginAt(LocalDateTime.now());
        save(admin);
        return buildLoginVO(admin);
    }

    @Override
    public LoginVO login(LoginRequest request) {
        SysAdmin admin = getOne(new LambdaQueryWrapper<SysAdmin>()
                .eq(SysAdmin::getUsername, request.getUsername())
                .last("limit 1"));
        if (admin == null) {
            throw new BusinessException("管理员账号不存在");
        }
        if (!Integer.valueOf(1).equals(admin.getStatus())) {
            throw new BusinessException("管理员账号已停用");
        }
        if (!PasswordUtil.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }
        admin.setLastLoginAt(LocalDateTime.now());
        updateById(admin);
        return buildLoginVO(admin);
    }

    @Override
    public SysAdmin getCurrentAdmin(Long adminId) {
        SysAdmin admin = getById(adminId);
        if (admin == null || !Integer.valueOf(1).equals(admin.getStatus())) {
            throw new BusinessException("管理员不存在或已停用");
        }
        return admin;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysAdmin updateProfile(Long adminId, AdminProfileUpdateRequest request) {
        SysAdmin admin = getCurrentAdmin(adminId);

        String phone = StringUtils.trimToNull(request.getPhone());
        if (StringUtils.isNotBlank(phone)
                && !StringUtils.equals(phone, admin.getPhone())
                && count(new LambdaQueryWrapper<SysAdmin>()
                .eq(SysAdmin::getPhone, phone)
                .ne(SysAdmin::getAdminId, adminId)) > 0) {
            throw new BusinessException("手机号已被其他管理员使用");
        }

        String password = StringUtils.trimToNull(request.getPassword());
        String confirmPassword = StringUtils.trimToNull(request.getConfirmPassword());
        if (StringUtils.isNotBlank(password) || StringUtils.isNotBlank(confirmPassword)) {
            if (!StringUtils.equals(password, confirmPassword)) {
                throw new BusinessException("两次输入的新密码不一致");
            }
            admin.setPasswordHash(PasswordUtil.encode(password));
        }

        admin.setRealName(StringUtils.defaultIfBlank(StringUtils.trimToNull(request.getRealName()), admin.getRealName()));
        admin.setPhone(phone);
        admin.setEmail(StringUtils.trimToNull(request.getEmail()));
        admin.setIdCardNo(StringUtils.trimToNull(request.getIdCardNo()));
        updateById(admin);
        return admin;
    }

    private LoginVO buildLoginVO(SysAdmin admin) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(admin.getAdminId());
        loginUser.setUsername(admin.getUsername());
        loginUser.setDisplayName(StringUtils.defaultIfBlank(admin.getRealName(), admin.getUsername()));
        loginUser.setRoleType(RoleType.ADMIN);

        LoginVO loginVO = new LoginVO();
        loginVO.setUserId(admin.getAdminId());
        loginVO.setUsername(admin.getUsername());
        loginVO.setDisplayName(StringUtils.defaultIfBlank(admin.getRealName(), admin.getUsername()));
        loginVO.setRoleType(RoleType.ADMIN);
        loginVO.setToken(jwtUtil.generateToken(loginUser));
        return loginVO;
    }
}
