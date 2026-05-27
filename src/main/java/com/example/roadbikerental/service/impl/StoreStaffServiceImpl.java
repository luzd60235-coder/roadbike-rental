package com.example.roadbikerental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.roadbikerental.common.auth.LoginUser;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.exception.BusinessException;
import com.example.roadbikerental.common.result.PageResult;
import com.example.roadbikerental.common.util.JwtUtil;
import com.example.roadbikerental.common.util.PasswordUtil;
import com.example.roadbikerental.dto.LoginRequest;
import com.example.roadbikerental.dto.admin.AdminStoreStaffUpdateRequest;
import com.example.roadbikerental.dto.staff.StaffProfileUpdateRequest;
import com.example.roadbikerental.dto.staff.StaffRegisterRequest;
import com.example.roadbikerental.entity.MaintenanceRecord;
import com.example.roadbikerental.entity.RentalOrder;
import com.example.roadbikerental.entity.RentalOrderSettlement;
import com.example.roadbikerental.entity.RentalStore;
import com.example.roadbikerental.entity.StoreStaff;
import com.example.roadbikerental.mapper.MaintenanceRecordMapper;
import com.example.roadbikerental.mapper.RentalOrderMapper;
import com.example.roadbikerental.mapper.RentalOrderSettlementMapper;
import com.example.roadbikerental.mapper.StoreStaffMapper;
import com.example.roadbikerental.service.RentalStoreService;
import com.example.roadbikerental.service.StoreStaffService;
import com.example.roadbikerental.vo.LoginVO;
import com.example.roadbikerental.vo.admin.AdminStoreStaffVO;
import com.example.roadbikerental.vo.staff.StaffProfileVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 门店员工服务实现。
 */
@Service
public class StoreStaffServiceImpl extends ServiceImpl<StoreStaffMapper, StoreStaff> implements StoreStaffService {

    private final JwtUtil jwtUtil;
    private final RentalStoreService rentalStoreService;
    private final RentalOrderMapper rentalOrderMapper;
    private final RentalOrderSettlementMapper rentalOrderSettlementMapper;
    private final MaintenanceRecordMapper maintenanceRecordMapper;

    public StoreStaffServiceImpl(JwtUtil jwtUtil,
                                 RentalStoreService rentalStoreService,
                                 RentalOrderMapper rentalOrderMapper,
                                 RentalOrderSettlementMapper rentalOrderSettlementMapper,
                                 MaintenanceRecordMapper maintenanceRecordMapper) {
        this.jwtUtil = jwtUtil;
        this.rentalStoreService = rentalStoreService;
        this.rentalOrderMapper = rentalOrderMapper;
        this.rentalOrderSettlementMapper = rentalOrderSettlementMapper;
        this.maintenanceRecordMapper = maintenanceRecordMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(StaffRegisterRequest request) {
        if (!StringUtils.equals(request.getPassword(), request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        if (count(new LambdaQueryWrapper<StoreStaff>().eq(StoreStaff::getUsername, request.getUsername())) > 0) {
            throw new BusinessException("用户名已存在");
        }
        if (StringUtils.isNotBlank(request.getPhone())
                && count(new LambdaQueryWrapper<StoreStaff>().eq(StoreStaff::getPhone, request.getPhone())) > 0) {
            throw new BusinessException("手机号已注册");
        }

        RentalStore store = rentalStoreService.getById(request.getStoreId());
        if (store == null || !Integer.valueOf(1).equals(store.getStatus())) {
            throw new BusinessException("门店不存在或已停用");
        }

        StoreStaff staff = new StoreStaff();
        staff.setStoreId(request.getStoreId());
        staff.setUsername(request.getUsername());
        staff.setPasswordHash(PasswordUtil.encode(request.getPassword()));
        staff.setStaffName(request.getStaffName());
        staff.setGender(request.getGender());
        staff.setPhone(StringUtils.trimToNull(request.getPhone()));
        staff.setIdCardNo(StringUtils.trim(request.getIdCardNo()));
        staff.setJobTitle(StringUtils.defaultIfBlank(StringUtils.trimToNull(request.getJobTitle()), "clerk"));
        staff.setWorkSchedule(StringUtils.defaultIfBlank(
                StringUtils.trimToNull(store.getBusinessHours()),
                "按门店排班"));
        staff.setStatus(1);
        staff.setLastLoginAt(LocalDateTime.now());
        save(staff);
        return buildLoginVO(staff, store);
    }

    @Override
    public LoginVO login(LoginRequest request) {
        StoreStaff staff = getOne(new LambdaQueryWrapper<StoreStaff>()
                .eq(StoreStaff::getUsername, request.getUsername())
                .last("limit 1"));
        if (staff == null) {
            throw new BusinessException("员工账号不存在");
        }
        if (!Integer.valueOf(1).equals(staff.getStatus())) {
            throw new BusinessException("员工账号已停用");
        }
        if (!PasswordUtil.matches(request.getPassword(), staff.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }
        staff.setLastLoginAt(LocalDateTime.now());
        updateById(staff);
        return buildLoginVO(staff, rentalStoreService.getById(staff.getStoreId()));
    }

    @Override
    public StoreStaff getRequiredById(Long staffId) {
        StoreStaff staff = getById(staffId);
        if (staff == null || !Integer.valueOf(1).equals(staff.getStatus())) {
            throw new BusinessException("员工不存在或已停用");
        }
        return staff;
    }

    @Override
    public StaffProfileVO getProfile(Long staffId) {
        StoreStaff staff = getRequiredById(staffId);
        RentalStore store = rentalStoreService.getById(staff.getStoreId());
        return buildProfileVO(staff, store);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StaffProfileVO updateProfile(Long staffId, StaffProfileUpdateRequest request) {
        StoreStaff staff = getRequiredById(staffId);

        String phone = StringUtils.trimToNull(request.getPhone());
        if (StringUtils.isNotBlank(phone)
                && !StringUtils.equals(phone, staff.getPhone())
                && count(new LambdaQueryWrapper<StoreStaff>()
                .eq(StoreStaff::getPhone, phone)
                .ne(StoreStaff::getStaffId, staffId)) > 0) {
            throw new BusinessException("手机号已被其他员工使用");
        }

        String password = StringUtils.trimToNull(request.getPassword());
        String confirmPassword = StringUtils.trimToNull(request.getConfirmPassword());
        if (StringUtils.isNotBlank(password) || StringUtils.isNotBlank(confirmPassword)) {
            if (!StringUtils.equals(password, confirmPassword)) {
                throw new BusinessException("两次输入的密码不一致");
            }
            staff.setPasswordHash(PasswordUtil.encode(password));
        }

        if (StringUtils.isNotBlank(request.getStaffName())) {
            staff.setStaffName(StringUtils.trim(request.getStaffName()));
        }
        staff.setPhone(phone);

        updateById(staff);
        return buildProfileVO(staff, rentalStoreService.getById(staff.getStoreId()));
    }

    @Override
    public PageResult<AdminStoreStaffVO> pageAdminStaffs(Long current, Long size, String keyword, Long storeId, Integer status) {
        LambdaQueryWrapper<StoreStaff> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(storeId != null, StoreStaff::getStoreId, storeId)
                .eq(status != null, StoreStaff::getStatus, status)
                .and(StringUtils.isNotBlank(keyword), wrapper -> wrapper
                        .like(StoreStaff::getUsername, keyword)
                        .or()
                        .like(StoreStaff::getStaffName, keyword)
                        .or()
                        .like(StoreStaff::getPhone, keyword)
                        .or()
                        .like(StoreStaff::getJobTitle, keyword))
                .orderByDesc(StoreStaff::getCreatedAt);

        Page<StoreStaff> page = page(new Page<>(current, size), queryWrapper);
        Page<AdminStoreStaffVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(page.getRecords().stream()
                .map(this::buildAdminStaffVO)
                .collect(java.util.stream.Collectors.toList()));
        return PageResult.of(resultPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminStoreStaffVO updateAdminStaff(Long staffId, AdminStoreStaffUpdateRequest request) {
        StoreStaff staff = getById(staffId);
        if (staff == null) {
            throw new BusinessException("门店员工不存在");
        }

        String phone = StringUtils.trimToNull(request.getPhone());
        if (StringUtils.isNotBlank(phone)
                && !StringUtils.equals(phone, staff.getPhone())
                && count(new LambdaQueryWrapper<StoreStaff>()
                .eq(StoreStaff::getPhone, phone)
                .ne(StoreStaff::getStaffId, staffId)) > 0) {
            throw new BusinessException("手机号已被其他员工使用");
        }

        String password = StringUtils.trimToNull(request.getPassword());
        String confirmPassword = StringUtils.trimToNull(request.getConfirmPassword());
        if (StringUtils.isNotBlank(password) || StringUtils.isNotBlank(confirmPassword)) {
            if (!StringUtils.equals(password, confirmPassword)) {
                throw new BusinessException("两次输入的新密码不一致");
            }
            staff.setPasswordHash(PasswordUtil.encode(password));
        }

        if (StringUtils.isNotBlank(request.getStaffName())) {
            staff.setStaffName(StringUtils.trim(request.getStaffName()));
        }
        if (request.getPhone() != null) {
            staff.setPhone(phone);
        }
        if (StringUtils.isNotBlank(request.getJobTitle())) {
            staff.setJobTitle(StringUtils.trim(request.getJobTitle()));
        }
        if (request.getWorkSchedule() != null) {
            staff.setWorkSchedule(StringUtils.defaultIfBlank(StringUtils.trimToNull(request.getWorkSchedule()), "按门店排班"));
        }
        if (request.getStatus() != null) {
            staff.setStatus(request.getStatus());
        }

        updateById(staff);
        return buildAdminStaffVO(staff);
    }

    private LoginVO buildLoginVO(StoreStaff staff, RentalStore store) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(staff.getStaffId());
        loginUser.setUsername(staff.getUsername());
        loginUser.setDisplayName(staff.getStaffName());
        loginUser.setRoleType(RoleType.STAFF);
        loginUser.setStoreId(staff.getStoreId());

        LoginVO loginVO = new LoginVO();
        loginVO.setUserId(staff.getStaffId());
        loginVO.setUsername(staff.getUsername());
        loginVO.setDisplayName(staff.getStaffName());
        loginVO.setRoleType(RoleType.STAFF);
        loginVO.setStoreId(staff.getStoreId());
        loginVO.setStoreName(store == null ? null : store.getStoreName());
        loginVO.setToken(jwtUtil.generateToken(loginUser));
        return loginVO;
    }

    private StaffProfileVO buildProfileVO(StoreStaff staff, RentalStore store) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        StaffProfileVO profile = new StaffProfileVO();
        profile.setStaffId(staff.getStaffId());
        profile.setUsername(staff.getUsername());
        profile.setStaffName(staff.getStaffName());
        profile.setPhone(staff.getPhone());
        profile.setJobTitle(staff.getJobTitle());
        profile.setWorkSchedule(staff.getWorkSchedule());
        profile.setStatus(staff.getStatus());
        profile.setStoreId(staff.getStoreId());
        profile.setStoreName(store == null ? null : store.getStoreName());
        profile.setStorePhone(store == null ? null : store.getContactPhone());
        profile.setBusinessHours(store == null ? null : store.getBusinessHours());
        profile.setStoreAddress(buildStoreAddress(store));
        profile.setCurrentShift(resolveShiftName());
        profile.setShiftPeriod(StringUtils.defaultIfBlank(
                StringUtils.trimToNull(staff.getWorkSchedule()),
                store == null
                        ? "当前按门店安排值班"
                        : StringUtils.defaultIfBlank(store.getBusinessHours(), "当前按门店安排值班")));
        profile.setLastLoginAt(staff.getLastLoginAt());
        profile.setCreatedAt(staff.getCreatedAt());
        profile.setPendingPickupCount(countPickupStoreOrders(staff.getStoreId(), 2));
        profile.setRentingCount(countPickupStoreOrders(staff.getStoreId(), 3));
        profile.setPendingSettlementCount(countReturnStoreOrders(staff.getStoreId(), 4));
        profile.setMaintenanceInProgressCount(countStoreMaintenance(staff.getStoreId(), 2, null, null, null));
        profile.setCreatedOrdersToday(countCreatedOrdersToday(staff.getStaffId(), startOfDay, endOfDay));
        profile.setSettlementsToday(countSettlementsToday(staff.getStaffId(), startOfDay, endOfDay));
        profile.setCompletedMaintenanceToday(countStoreMaintenance(
                staff.getStoreId(), 3, staff.getStaffId(), startOfDay, endOfDay));
        return profile;
    }

    private AdminStoreStaffVO buildAdminStaffVO(StoreStaff staff) {
        RentalStore store = rentalStoreService.getById(staff.getStoreId());
        AdminStoreStaffVO vo = new AdminStoreStaffVO();
        vo.setStaffId(staff.getStaffId());
        vo.setStoreId(staff.getStoreId());
        vo.setStoreName(store == null ? null : store.getStoreName());
        vo.setStoreCity(store == null ? null : store.getCity());
        vo.setStoreDistrict(store == null ? null : store.getDistrict());
        vo.setBusinessHours(store == null ? null : store.getBusinessHours());
        vo.setUsername(staff.getUsername());
        vo.setStaffName(staff.getStaffName());
        vo.setPhone(staff.getPhone());
        vo.setJobTitle(staff.getJobTitle());
        vo.setWorkSchedule(staff.getWorkSchedule());
        vo.setStatus(staff.getStatus());
        vo.setLastLoginAt(staff.getLastLoginAt());
        vo.setCreatedAt(staff.getCreatedAt());
        return vo;
    }

    private Long countPickupStoreOrders(Long storeId, Integer status) {
        return rentalOrderMapper.selectCount(new LambdaQueryWrapper<RentalOrder>()
                .eq(RentalOrder::getPickupStoreId, storeId)
                .eq(RentalOrder::getOrderStatus, status));
    }

    private Long countReturnStoreOrders(Long storeId, Integer status) {
        return rentalOrderMapper.selectCount(new LambdaQueryWrapper<RentalOrder>()
                .eq(RentalOrder::getReturnStoreId, storeId)
                .eq(RentalOrder::getOrderStatus, status));
    }

    private Long countCreatedOrdersToday(Long staffId, LocalDateTime start, LocalDateTime end) {
        return rentalOrderMapper.selectCount(new LambdaQueryWrapper<RentalOrder>()
                .eq(RentalOrder::getCreatedStaffId, staffId)
                .ge(RentalOrder::getCreatedAt, start)
                .lt(RentalOrder::getCreatedAt, end));
    }

    private Long countSettlementsToday(Long staffId, LocalDateTime start, LocalDateTime end) {
        return rentalOrderSettlementMapper.selectCount(new LambdaQueryWrapper<RentalOrderSettlement>()
                .eq(RentalOrderSettlement::getSettleStaffId, staffId)
                .ge(RentalOrderSettlement::getSettledAt, start)
                .lt(RentalOrderSettlement::getSettledAt, end));
    }

    private Long countStoreMaintenance(Long storeId,
                                       Integer status,
                                       Long assignedStaffId,
                                       LocalDateTime start,
                                       LocalDateTime end) {
        LambdaQueryWrapper<MaintenanceRecord> wrapper = new LambdaQueryWrapper<MaintenanceRecord>()
                .eq(MaintenanceRecord::getStoreId, storeId)
                .eq(MaintenanceRecord::getMaintenanceStatus, status);
        if (assignedStaffId != null) {
            wrapper.eq(MaintenanceRecord::getAssignedStaffId, assignedStaffId);
        }
        if (start != null) {
            wrapper.ge(MaintenanceRecord::getEndTime, start);
        }
        if (end != null) {
            wrapper.lt(MaintenanceRecord::getEndTime, end);
        }
        return maintenanceRecordMapper.selectCount(wrapper);
    }

    private String resolveShiftName() {
        LocalTime now = LocalTime.now();
        if (now.isBefore(LocalTime.NOON)) {
            return "早班值守";
        }
        if (now.isBefore(LocalTime.of(18, 0))) {
            return "白班值守";
        }
        return "晚班值守";
    }

    private String buildStoreAddress(RentalStore store) {
        if (store == null) {
            return "-";
        }
        StringBuilder builder = new StringBuilder();
        appendAddressPart(builder, store.getProvince());
        appendAddressPart(builder, store.getCity());
        appendAddressPart(builder, store.getDistrict());
        appendAddressPart(builder, store.getDetailAddress());
        return StringUtils.defaultIfBlank(builder.toString().trim(), "-");
    }

    private void appendAddressPart(StringBuilder builder, String value) {
        String text = StringUtils.trimToNull(value);
        if (text != null) {
            builder.append(text);
        }
    }
}
