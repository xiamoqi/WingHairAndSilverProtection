package com.yiguardsilverfa.dto.medicineInfo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MedicineUpdateDTO {
    private Long id;                // 药品记录ID
    private String medicineName;
    private LocalDate expiryDate;
    private String type;
    private Integer quantity;
    private String remark;
}
