package com.yiguardsilverfa.dto.medicineRemind;

import lombok.Data;
import java.time.LocalTime;

/**
 * 添加的用药提醒
 */
@Data
public class MedicineRemindAddDTO {
    private Long elderId;           //绑定的老人ID（家属添加时必须传，老人添加可不传）
    private String medicineName;

    /**
     * 用量
     */
    private String dosage;
    /**
     * 用法
     */
    private String usage;
    /**
     * 提醒时间
     */
    private LocalTime remindTime;
    /**
     * 提醒日期
     */
    private String remindDays;
}
