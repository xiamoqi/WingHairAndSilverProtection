package com.yiguardsilverfa.dto.healthQa;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 保存查询的问答记录
 * 同时加上对应的名字
 */
@Data
public class SelectHealthQaDTO {
    private Long id;
    private Long elderId;
    private String elderName;//老人名字
    private String question;
    private String answer;
    private Integer isEmergency;
    private LocalDateTime askTime;
}
