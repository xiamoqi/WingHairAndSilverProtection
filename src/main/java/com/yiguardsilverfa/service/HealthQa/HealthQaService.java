package com.yiguardsilverfa.service.HealthQa;

import com.yiguardsilverfa.dto.healthQa.SelectHealthQaDTO;
import com.yiguardsilverfa.entity.HealthQa;
import com.yiguardsilverfa.entity.Result;

import java.util.List;

public interface HealthQaService {
    //家属获取指定老人姓名的提问记录
    Result<?> getElderQaByElderName(String elderName);
    //家属获取所有绑定老人的提问记录
    Result<?> getAllElderQuestions();
}
