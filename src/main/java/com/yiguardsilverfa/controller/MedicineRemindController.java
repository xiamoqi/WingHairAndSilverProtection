package com.yiguardsilverfa.controller;

import com.yiguardsilverfa.dto.medicineRemind.MedicineRemindAddDTO;
import com.yiguardsilverfa.dto.medicineRemind.MedicineRemindQueryDTO;
import com.yiguardsilverfa.dto.medicineRemind.MedicineRemindUpdateDTO;
import com.yiguardsilverfa.entity.MedicineRemind;
import com.yiguardsilverfa.entity.Result;
import com.yiguardsilverfa.service.MedicineRemind.MedicineRemindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicine-remind")
public class MedicineRemindController {
    @Autowired
    private MedicineRemindService medicineRemindService;

    @PostMapping("/add")
    public Result<?> addRemind(@RequestBody MedicineRemindAddDTO addDTO) {
        if (addDTO.getMedicineName()==null||addDTO.getMedicineName().trim().length()==0){
            return Result.failure("请输入药物的名字");
        }
        return medicineRemindService.addRemind(addDTO);
    }

    @PutMapping("/{id}")
    public Result<?> updateRemind(@RequestBody MedicineRemindUpdateDTO updateDTO) {
        if (updateDTO.getId()==null){
            return Result.failure("请选择要修改的提醒");
        }
        return medicineRemindService.updateRemind(updateDTO);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteRemind(@PathVariable Long id) {
        if (id==null){
            return Result.failure("请选择要删除的提醒");
        }
        return medicineRemindService.deleteRemind(id);
    }

    @GetMapping("/my-reminds")
    public Result<List<MedicineRemind>> getMyReminds(){
        List<MedicineRemind> list = medicineRemindService.getMyReminds();
        return Result.success(list);
    }

}
