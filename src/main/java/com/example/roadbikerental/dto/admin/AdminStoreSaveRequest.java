package com.example.roadbikerental.dto.admin;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 后台门店保存请求。
 */
@Data
public class AdminStoreSaveRequest {

    private Long storeId;

    @NotBlank(message = "门店名称不能为空")
    private String storeName;

    private String contactName;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    private String province;

    private String city;

    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String detailAddress;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String storeImageUrl;

    private String businessHours;

    @NotNull(message = "库存容量不能为空")
    private Integer inventoryCapacity;

    private String description;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
