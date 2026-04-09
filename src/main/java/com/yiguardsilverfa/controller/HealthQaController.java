package com.yiguardsilverfa.controller;

import com.yiguardsilverfa.dto.healthQa.SelectHealthQaDTO;
import com.yiguardsilverfa.entity.Result;
import com.yiguardsilverfa.service.HealthQa.HealthQaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/health-qa")
public class HealthQaController {
    @Autowired
    private HealthQaService healthQaService;

    /**
     * 家属获取所有绑定老人的提问记录
     */
    @PostMapping("/family/questions")
    public Result<?> getFamilyQuestions() {
        List<SelectHealthQaDTO> list = healthQaService.getAllElderQuestions();
        return Result.success(list);
    }

    /**
     *  家属根据老人姓名查询该老人的问答记录
     */
    @PostMapping("/family/questions/by-eldername")
    public Result<?> getFamilyQuestionsByElderId(@RequestParam String eldername) {
        if (eldername == null || eldername.trim().isEmpty()) {
            return Result.failure("用户名不能为空");
        }
        List<SelectHealthQaDTO> list = healthQaService.getElderQaByElderName(eldername.trim());
        return Result.success(list);
    }
}
