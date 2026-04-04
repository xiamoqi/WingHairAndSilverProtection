package com.yiguardsilverfa.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HealthQa {
    private Long id;
    private Long elderId;
    private String question;
    private String answer;
    private Integer isEmergency;
    private LocalDateTime askTime;
}