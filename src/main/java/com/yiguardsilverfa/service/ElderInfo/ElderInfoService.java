package com.yiguardsilverfa.service.ElderInfo;

import com.yiguardsilverfa.dto.elder.ElderInfoAddDTO;
import com.yiguardsilverfa.dto.elder.ElderInfoUpdateDTO;
import com.yiguardsilverfa.dto.familyBind.BindElderAccountDTO;
import com.yiguardsilverfa.dto.user.SearchUserInfoDTO;
import com.yiguardsilverfa.entity.ElderInfo;

import java.util.List;

public interface ElderInfoService {
    void addElderInfo(ElderInfoAddDTO addDTO);
    void updateElderInfo(ElderInfoUpdateDTO updateDTO);
    //家属删除关系
    void deleteElderInfo(Long id);
    ElderInfo getElderInfoById(Long id);
    List<ElderInfo> getElderInfoByUserId(Long userId);

    //管理员恢复删除
    void restoreElderInfo(Long id);

    //通过username查找对应的user信息
    SearchUserInfoDTO getUserByUsername(String username);
    //绑定档案所对应的老人账号
    void bindElderAccount(BindElderAccountDTO bindDTO);

}
