package com.yiguardsilverfa.dto.medicineRemind;

import lombok.Data;
import java.time.LocalTime;
@Data
public class MedicineRemindUpdateDTO {
    private Long id;
    private String medicineName;
    private String dosage;
    private String usage;
    private LocalTime remindTime;
    private String remindDays;
    private Integer status;
}
