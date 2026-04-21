package com.yiguardsilverfa.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WarningEvent {
    private Long id;
    private Long elderId;
    private Integer type;//1-跌倒 2-紧急求助 3-环境异常 4-健康异常
    private String content;
    private String location;
    private Integer handleStatus;//0-未处理 1-已处理
    private Long handleUser;//处理人
    private LocalDateTime handleTime;
    private LocalDateTime createTime;
}