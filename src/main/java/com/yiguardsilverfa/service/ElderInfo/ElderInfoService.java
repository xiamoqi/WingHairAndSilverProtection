package com.yiguardsilverfa.service.ElderInfo;

import com.yiguardsilverfa.dto.elder.ElderInfoAddDTO;
import com.yiguardsilverfa.dto.elder.ElderInfoUpdateDTO;
import com.yiguardsilverfa.entity.ElderInfo;

public interface ElderInfoService {
    void addElderInfo(ElderInfoAddDTO addDTO);
    void updateElderInfo(ElderInfoUpdateDTO updateDTO);
    //家属删除关系
    void deleteElderInfo(Long id);
    ElderInfo getElderInfoById(Long id);
    ElderInfo getElderInfoByUserId(Long userId);

    //管理员恢复删除
    void restoreElderInfo(Long id);
}
