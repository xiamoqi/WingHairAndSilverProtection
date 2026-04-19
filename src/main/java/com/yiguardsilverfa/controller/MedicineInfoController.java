package com.yiguardsilverfa.controller;


import com.yiguardsilverfa.dto.medicineInfo.MedicineAddDTO;
import com.yiguardsilverfa.dto.medicineInfo.MedicineSelectDTO;
import com.yiguardsilverfa.dto.medicineInfo.MedicineUpdateDTO;
import com.yiguardsilverfa.entity.MedicineInfo;
import com.yiguardsilverfa.entity.Result;
import com.yiguardsilverfa.exception.BusinessException;
import com.yiguardsilverfa.service.MedicineInfo.MedicineInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicine")
public class MedicineInfoController {
    @Autowired
    private MedicineInfoService medicineInfoService;
    /**
     * 添加药品信息
     * 老人：无需传 elderId；家属：需传 elderId（老人用户ID）
     */
    @PostMapping("/add")
    public Result<?> addMedicine(@RequestBody MedicineAddDTO addDTO) {
        if(addDTO.getMedicineName()==null||addDTO.getMedicineName().trim().length()==0 ){
            return Result.failure("药品名称不能为空");
        }
        if(addDTO.getQuantity()<0){
            return Result.failure("药品数量不能小于0");
        }
        return medicineInfoService.addMedicineInfo(addDTO);

    }

    /**
     * 修改药品信息
     */
    @PostMapping("/update")
    public Result<?> updateMedicine(@RequestBody MedicineUpdateDTO updateDTO) {
        if (updateDTO.getId() == null) {
            return Result.failure("药品ID不能为空");
        }
        if(updateDTO.getQuantity()!=null&&updateDTO.getQuantity()<0){
            return Result.failure("药品数量不能小于0");
        }
        return medicineInfoService.updateMedicineInfo(updateDTO);

    }

    /**
     * 删除药品信息
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteMedicine(@PathVariable Long id) {
        if (id == null) {
            throw new BusinessException("药品ID不能为空");
        }
        return medicineInfoService.deleteMedicineInfo(id);

    }

    /**
     * 根据老人用户ID查询药品列表
     */
    @GetMapping("/list/elder/{elderId}")
    public Result<?> getByElderId(@PathVariable Long elderId) {
        return medicineInfoService.getMedicineInfoByElderId(elderId);
    }

    /**
     * 老人获取自己所有的药品信息（无需参数）
     */
    @GetMapping("/my-list")
    public Result<?> getMyMedicineList() {
        return medicineInfoService.getMyMedicineList();
    }

    /**
     * 条件查询药品（支持药品名称模糊查询、类型筛选）
     * 自动根据当前用户角色返回可访问的药品
     */
    @PostMapping("/list")
    public Result<?> getByCondition(@RequestBody(required = false) MedicineSelectDTO selectDTO) {
        if (selectDTO == null) {
            selectDTO = new MedicineSelectDTO();
        }
        return medicineInfoService.getMedicineInfoByCondition(selectDTO);
    }
}
