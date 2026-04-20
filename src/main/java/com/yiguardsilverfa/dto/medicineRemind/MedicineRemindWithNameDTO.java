package com.yiguardsilverfa.dto.medicineRemind;
import com.yiguardsilverfa.entity.MedicineRemind;
import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * 可以返回提醒相关联的老人姓名
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MedicineRemindWithNameDTO extends MedicineRemind {
    private String elderName; // 老人姓名
}
