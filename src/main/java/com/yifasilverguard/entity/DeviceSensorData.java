package com.yifasilverguard.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DeviceSensorData {
    private Long id;
    private String deviceId;
    private Long elderId;
    private BigDecimal temperature;
    private Integer humidity;
    private Integer gasStatus;
    private Integer isMove;
    private Integer fallStatus;
    private LocalDateTime reportTime;
}