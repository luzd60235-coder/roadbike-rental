package com.example.roadbikerental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 门店实体。
 */
@Data
@TableName("rental_store")
public class RentalStore implements Serializable {

    @TableId(value = "store_id", type = IdType.AUTO)
    private Long storeId;

    private String storeName;

    private String contactName;

    private String contactPhone;

    private String province;

    private String city;

    private String district;

    private String detailAddress;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String storeImageUrl;

    private String businessHours;

    private Integer inventoryCapacity;

    private String description;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
