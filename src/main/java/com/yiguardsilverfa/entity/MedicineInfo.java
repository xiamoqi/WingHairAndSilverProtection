package com.yiguardsilverfa.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MedicineInfo {
    /**
     * 药品信息ID，自增
     */
    private Long id;
    /**
     * 存老人档案id
     */
    private Long elderId;

    private String medicineName;    // 药品名称

    private LocalDate expiryDate;   // 有效期
    /**
     * 药品类型
     * 感冒发烧、肠胃消化、咳嗽咽痛、皮肤骨科、慢病用药、儿童用药、未分类
     */
    private String type;
    private Integer quantity;       // 数量
    private String remark;          // 备注
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer status;//状态 0-禁用 1-正常
}
