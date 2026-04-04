package com.yiguardsilverfa.dao;

import com.yiguardsilverfa.entity.FamilyBind;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FamilyBindDAO {
    void insert(FamilyBind familyBind);
    void update(FamilyBind familyBind);
    FamilyBind selectById(Long id);
    List<FamilyBind> selectByFamilyUserId(@Param("familyUserId") Long familyUserId);
    List<FamilyBind> selectByElderId(@Param("elderId") Long elderId);
    //根据家属ID和老人ID更新状态为0（解除绑定)
    void deleteByFamilyAndElder(@Param("familyUserId") Long familyUserId, @Param("elderId") Long elderId);
}