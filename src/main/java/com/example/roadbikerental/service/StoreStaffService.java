package com.example.roadbikerental.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.dto.LoginRequest;
import com.example.roadbikerental.dto.admin.AdminStoreStaffUpdateRequest;
import com.example.roadbikerental.dto.staff.StaffProfileUpdateRequest;
import com.example.roadbikerental.dto.staff.StaffRegisterRequest;
import com.example.roadbikerental.entity.StoreStaff;
import com.example.roadbikerental.vo.LoginVO;
import com.example.roadbikerental.vo.admin.AdminStoreStaffVO;
import com.example.roadbikerental.vo.staff.StaffProfileVO;

/**
 * 门店员工服务。
 */
public interface StoreStaffService extends IService<StoreStaff> {

    LoginVO register(StaffRegisterRequest request);

    LoginVO login(LoginRequest request);

    StoreStaff getRequiredById(Long staffId);

    StaffProfileVO getProfile(Long staffId);

    StaffProfileVO updateProfile(Long staffId, StaffProfileUpdateRequest request);

    PageResult<AdminStoreStaffVO> pageAdminStaffs(Long current, Long size, String keyword, Long storeId, Integer status);

    AdminStoreStaffVO updateAdminStaff(Long staffId, AdminStoreStaffUpdateRequest request);
}
