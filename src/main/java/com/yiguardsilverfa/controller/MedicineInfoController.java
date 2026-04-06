package com.yiguardsilverfa.controller;


import com.yiguardsilverfa.dto.medicineInfo.MedicineAddDTO;
import com.yiguardsilverfa.dto.medicineInfo.MedicineSelectDTO;
import com.yiguardsilverfa.dto.medicineInfo.MedicineUpdateDTO;
import com.yiguardsilverfa.entity.MedicineInfo;
import com.yiguardsilverfa.entity.Result;
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
        Boolean success = medicineInfoService.addMedicineInfo(addDTO);
        if (success) {
            return Result.success("添加成功");
        } else {
            return Result.failure("添加失败");
        }
    }

    /**
     * 修改药品信息
     */
    @PostMapping("/update")
    public Result<?> updateMedicine(@RequestBody MedicineUpdateDTO updateDTO) {
        Boolean success = medicineInfoService.updateMedicineInfo(updateDTO);
        if (success) {
            return Result.success("修改成功");
        } else {
            return Result.failure("修改失败");
        }
    }

    /**
     * 删除药品信息
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> deleteMedicine(@PathVariable Long id) {
        Boolean success = medicineInfoService.deleteMedicineInfo(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.failure("删除失败");
        }
    }

    /**
     * 根据老人用户ID查询药品列表
     */
    @GetMapping("/list/elder/{elderId}")
    public Result<List<MedicineInfo>> getByElderId(@PathVariable Long elderId) {
        List<MedicineInfo> list = medicineInfoService.getMedicineInfoByElderId(elderId);
        return Result.success(list);
    }

    /**
     * 老人获取自己的药品列表（无需参数）
     */
    @GetMapping("/my-list")
    public Result<List<MedicineInfo>> getMyMedicineList() {
        List<MedicineInfo> list = medicineInfoService.getMyMedicineList();
        return Result.success(list);
    }

    /**
     * 条件查询药品（支持药品名称模糊查询、类型筛选）
     * 自动根据当前用户角色返回可访问的药品
     */
    @PostMapping("/list")
    public Result<List<MedicineInfo>> getByCondition(@RequestBody(required = false) MedicineSelectDTO selectDTO) {
        if (selectDTO == null) {
            selectDTO = new MedicineSelectDTO();
        }
        List<MedicineInfo> list = medicineInfoService.getMedicineInfoByCondition(selectDTO);
        return Result.success(list);
    }
}
