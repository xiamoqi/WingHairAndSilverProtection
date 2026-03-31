package com.yifasilverguard.dao;

import com.yifasilverguard.entity.ElderInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ElderInfoDAO {

    /**
     * 新增老人信息
     */
    void insertElderInfo(ElderInfo elderInfo);

    /**
     * 根据用户ID查询老人信息
     */
    ElderInfo selectElderInfoByUserId(Long userId);

    /**
     * 根据老人ID更新信息
     */
    void updateElderInfoById(ElderInfo elderInfo);
}