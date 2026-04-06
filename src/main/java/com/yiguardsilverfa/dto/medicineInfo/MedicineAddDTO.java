package com.yiguardsilverfa.dto.medicineInfo;

import lombok.Data;

import java.time.LocalDate;
@Data
public class MedicineAddDTO {
    /**
     * 老人用户id，
     * 老人添加时可以为，借当前用户id去查找eldrtinfo中对应的档案
     * 家属添加时不能为空，需要选择对应的老人信息去添加对应的药品
     */
    private Long elderId;
    private String medicineName;
    /**
     * 有效期
     */
    private LocalDate expiryDate;
    private String type;
    /**
     * 数量
     */
    private Integer quantity;
    private String remark;
}
