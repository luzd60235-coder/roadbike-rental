package com.example.roadbikerental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 后台管理员实体。
 */
@Data
@TableName("sys_admin")
public class SysAdmin implements Serializable {

    @TableId(value = "admin_id", type = IdType.AUTO)
    private Long adminId;

    private String username;

    private String passwordHash;

    private String realName;

    private String phone;

    private String email;

    private String idCardNo;

    private Integer status;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
