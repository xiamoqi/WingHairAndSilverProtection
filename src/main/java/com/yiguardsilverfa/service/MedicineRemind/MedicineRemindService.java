package com.yiguardsilverfa.service.MedicineRemind;

import com.yiguardsilverfa.dto.medicineRemind.MedicineRemindAddDTO;
import com.yiguardsilverfa.dto.medicineRemind.MedicineRemindQueryDTO;
import com.yiguardsilverfa.dto.medicineRemind.MedicineRemindUpdateDTO;
import com.yiguardsilverfa.dto.medicineRemind.MedicineRemindWithNameDTO;
import com.yiguardsilverfa.entity.MedicineRemind;
import com.yiguardsilverfa.entity.Result;

import java.util.List;

public interface MedicineRemindService {
    Result<?> addRemind(MedicineRemindAddDTO addDTO);
    Result<?> updateRemind(MedicineRemindUpdateDTO updateDTO);
    Result<?> deleteRemind(Long id);
    
    List<MedicineRemindWithNameDTO> getMyReminds(); // 老人自己的提醒

    MedicineRemind getById(Long id);

    List<MedicineRemind> getListByElderId(Long elderId);
}
