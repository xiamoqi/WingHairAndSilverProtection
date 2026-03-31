package com.yifasilverguard.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WarningEvent {
    private Long id;
    private Long elderId;
    private Integer type;
    private String content;
    private String location;
    private Integer handleStatus;
    private Long handleUser;
    private LocalDateTime handleTime;
    private LocalDateTime createTime;
}