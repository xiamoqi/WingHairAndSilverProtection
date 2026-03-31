package com.yifasilverguard.dao;

import com.yifasilverguard.entity.FamilyBind;
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
    void deleteByFamilyAndElder(@Param("familyUserId") Long familyUserId, @Param("elderId") Long elderId);
}