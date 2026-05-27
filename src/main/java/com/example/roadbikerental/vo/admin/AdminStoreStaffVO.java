package com.example.roadbikerental.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端门店员工视图对象。
 */
@Data
public class AdminStoreStaffVO {

    private Long staffId;

    private Long storeId;

    private String storeName;

    private String storeCity;

    private String storeDistrict;

    private String businessHours;

    private String username;

    private String staffName;

    private String phone;

    private String jobTitle;

    private String workSchedule;

    private Integer status;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;
}
