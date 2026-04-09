package com.yiguardsilverfa.dao;

import com.yiguardsilverfa.dto.familyBind.BindElderInfoDTO;
import com.yiguardsilverfa.entity.FamilyBind;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FamilyBindDAO {
    int insert(FamilyBind familyBind);
    void update(FamilyBind familyBind);
    FamilyBind selectById(Long id);
    List<FamilyBind> selectByFamilyUserId(@Param("familyUserId") Long familyUserId);
    List<FamilyBind> selectByElderId(@Param("elderId") Long elderId);
    //根据家属ID和老人ID更新状态为0（解除绑定)
    int deleteByFamilyAndElder(@Param("familyUserId") Long familyUserId, @Param("elderId") Long elderId);
    //根据家属ID和老人档案ID统计绑定数量（用于校验）
    int selectByFamilyAndElder(@Param("familyUserId") Long familyUserId, @Param("elderId") Long elderId);

    //获取所有绑定关系所有的档案信息
    List<BindElderInfoDTO> selectBoundEldersWithInfo(@Param("familyUserId") Long familyUserId);
}