package com.yiguardsilverfa.dao;

import com.yiguardsilverfa.dto.familyBind.BindElderInfoDTO;
import com.yiguardsilverfa.entity.FamilyBind;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FamilyBindDAO {
    int insert(FamilyBind familyBind);
    List<FamilyBind> selectByFamilyUserId(@Param("familyUserId") Long familyUserId);
    //根据家属ID和老人ID更新状态为0（解除绑定)
    int deleteByFamilyAndElder(@Param("familyUserId") Long familyUserId, @Param("elderId") Long elderId);
    //根据家属ID和老人档案ID统计绑定数量（用于校验）
    int selectByFamilyAndElder(@Param("familyUserId") Long familyUserId, @Param("elderUserId") Long elderUserId);
    void updateElderUserId(FamilyBind bind);
}