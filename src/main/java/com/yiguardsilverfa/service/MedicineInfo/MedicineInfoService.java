package com.yiguardsilverfa.service.MedicineInfo;

import com.yiguardsilverfa.dto.medicineInfo.MedicineAddDTO;
import com.yiguardsilverfa.dto.medicineInfo.MedicineSelectDTO;
import com.yiguardsilverfa.dto.medicineInfo.MedicineUpdateDTO;
import com.yiguardsilverfa.entity.MedicineInfo;
import com.yiguardsilverfa.entity.Result;

import java.util.List;

public interface MedicineInfoService {
    /**
     * 添加药品信息
     */
    Result<?> addMedicineInfo(MedicineAddDTO medicineAddDTO);
    /**
     * 修改药品信息
     */
    Result<?> updateMedicineInfo(MedicineUpdateDTO medicineUpdateDTO);
    /**
     * 删除药品信息
     */
    Result<?> deleteMedicineInfo(Long id);
    /**
     * 根据老人档案ID查询药品信息
     */
    List<MedicineInfo> getMedicineInfoByElderId(Long elderId);
    /**
     * 老人获取自己所有的药品信息
     */
    Result<?> getMyMedicineList();

    /**
     * 模糊查询药品信息
     */
    List<MedicineInfo> getMedicineInfoByCondition(MedicineSelectDTO medicineSelectDTO);
}
