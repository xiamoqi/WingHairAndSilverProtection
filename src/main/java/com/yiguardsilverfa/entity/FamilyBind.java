package com.yiguardsilverfa.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FamilyBind {
    private Long id;
    private Long familyUserId;
    private Long elderId;
    /**
     * 子女/配偶/其他
     */
    private String relation;
    private Integer status;
    private LocalDateTime createTime;
}