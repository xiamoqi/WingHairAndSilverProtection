package com.yiguardsilverfa.dto.medicineInfo;

import lombok.Data;

@Data
public class MedicineSelectDTO {
    private String medicineName;    // 模糊查询
    private String type;            // 精确类型
}
