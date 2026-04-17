package com.yiguardsilverfa.dto.medicineRemind;

import lombok.Data;

@Data
public class MedicineRemindQueryDTO {
    private String medicineName;    // 模糊查询
    private Integer status;
}
