package com.example.roadbikerental.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.roadbikerental.dto.LoginRequest;
import com.example.roadbikerental.dto.admin.AdminProfileUpdateRequest;
import com.example.roadbikerental.dto.admin.AdminRegisterRequest;
import com.example.roadbikerental.entity.SysAdmin;
import com.example.roadbikerental.vo.LoginVO;

/**
 * 后台管理员服务。
 */
public interface SysAdminService extends IService<SysAdmin> {

    LoginVO register(AdminRegisterRequest request);

    LoginVO login(LoginRequest request);

    SysAdmin getCurrentAdmin(Long adminId);

    SysAdmin updateProfile(Long adminId, AdminProfileUpdateRequest request);
}
