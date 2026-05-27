package com.example.roadbikerental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 门店员工实体。
 */
@Data
@TableName("store_staff")
public class StoreStaff implements Serializable {

    @TableId(value = "staff_id", type = IdType.AUTO)
    private Long staffId;

    private Long storeId;

    private String username;

    private String passwordHash;

    private String staffName;

    private Integer gender;

    private String phone;

    private String idCardNo;

    private String jobTitle;

    private String workSchedule;

    private Integer status;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
