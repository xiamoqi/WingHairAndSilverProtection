package com.yiguardsilverfa.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ServiceOrder {
    private Long id;
    private Long elderId;
    private Integer serviceType;
    private String content;
    private LocalDateTime appointTime;
    private Integer status;
    private LocalDateTime createTime;
}