package com.yiguardsilverfa.controller;

import com.yiguardsilverfa.dto.medicineRemind.MedicineRemindAddDTO;
import com.yiguardsilverfa.dto.medicineRemind.MedicineRemindQueryDTO;
import com.yiguardsilverfa.dto.medicineRemind.MedicineRemindUpdateDTO;
import com.yiguardsilverfa.entity.MedicineRemind;
import com.yiguardsilverfa.entity.Result;
import com.yiguardsilverfa.service.MedicineRemind.MedicineRemindService;
import com.yiguardsilverfa.utils.BaseContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicine-remind")
public class MedicineRemindController {
    @Autowired
    private MedicineRemindService medicineRemindService;

    @PostMapping
    public Result<?> addRemind(@RequestBody MedicineRemindAddDTO addDTO) {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        if (addDTO.getMedicineName()==null||addDTO.getMedicineName().trim().length()==0){
            return Result.failure("请输入药物的名字");
        }
        return medicineRemindService.addRemind(addDTO);
    }

    @PutMapping("/{id}")
    public Result<?> updateRemind(@PathVariable Long id,@RequestBody MedicineRemindUpdateDTO updateDTO) {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        if (id == null) {
            return Result.failure("请选择要修改的提醒");
        }
        updateDTO.setId(id);
        return medicineRemindService.updateRemind(updateDTO);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteRemind(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized();
        }
        if (id == null) {
            return Result.failure("请选择要删除的提醒");
        }
        return medicineRemindService.deleteRemind(id);
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        MedicineRemind remind = medicineRemindService.getById(id);
        return Result.success(remind);
    }

    @GetMapping("/my-list")
    public Result<List<MedicineRemind>> getMyReminds(){
        Long userId = BaseContext.getCurrentUserId();
        if (userId == null) {
            return Result.failure(null, "未登录");
        }
        List<MedicineRemind> list = medicineRemindService.getMyReminds();
        return Result.success(list);
    }

    @GetMapping("/elder/{elderId}")
    public Result<List<MedicineRemind>> getByElderId(@PathVariable Long elderId) {
        List<MedicineRemind> list = medicineRemindService.getListByElderId(elderId);
        return Result.success(list);
    }
}
