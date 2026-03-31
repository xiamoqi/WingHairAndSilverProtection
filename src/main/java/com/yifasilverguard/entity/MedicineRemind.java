package com.yifasilverguard.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class MedicineRemind {
    private Long id;
    private Long elderId;
    private String medicineName;
    private String dosage;
    private String usage;
    private LocalTime remindTime;
    private String remindDays;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}