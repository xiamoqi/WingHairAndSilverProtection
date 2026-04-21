package com.yiguardsilverfa.controller;

import com.yiguardsilverfa.dto.healthQa.ElderIdDTO;
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
        return healthQaService.getAllElderQuestions();
    }

    /**
     *  家属根据老人姓名查询该老人的问答记录
     */
    @PostMapping("/family/questions/by-elderid")
    public Result<?> getFamilyQuestionsByElderId(@RequestBody ElderIdDTO dto) {
        Long elderId = dto.getElderId();
        if (elderId == null || elderId <= 0) {
            return Result.failure("老人ID不能为空");
        }
        return healthQaService.getElderQaByElderId(elderId);
    }
}
