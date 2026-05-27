package com.example.roadbikerental.vo.user;

import lombok.Data;

import java.util.List;

@Data
public class UserBikeDetailVO {

    private UserBikeVO bike;

    private List<UserBikeMaintenanceVO> maintenanceHistory;
}
