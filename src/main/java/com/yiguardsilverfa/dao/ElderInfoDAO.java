package com.yiguardsilverfa.dao;

import com.yiguardsilverfa.entity.ElderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ElderInfoDAO {

    /**
     * 新增老人信息
     */
    int insertElderInfo(ElderInfo elderInfo);

    /**
     * 根据用户ID查询老人信息
     */
    List<ElderInfo> selectElderInfoByUserId(@Param("userId") Long userId);

    /**
     * 根据老人ID更新信息
     * 动态更新档案（只更新非空字段）
     */
    int updateElderInfoById(ElderInfo elderInfo);

    /**
     * 根据ID逻辑删除
     */
    int softDeleteById(@Param("id") Long id);
    /**
     * 根据userId逻辑删除
     */
    int softDeleteByUserId(@Param("userId") Long userId);
    /**
     * 根据ID查询
     */
    ElderInfo selectElderInfoById(@Param("id") Long id);

    /**
     * 管理员恢复删除
     */
    int restoreById(@Param("id") Long id);
}