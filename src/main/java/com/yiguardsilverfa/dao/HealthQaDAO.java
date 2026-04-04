package com.yiguardsilverfa.dao;

import com.yiguardsilverfa.entity.HealthQa;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HealthQaDAO {

    /**
     * 插入问答记录
     */
    void insert(HealthQa healthQa);

    /**
     * 根据老人ID查询问答记录
     */
    List<HealthQa> selectByElderId(@Param("elderId") Long elderId);

    /**
     * 查询紧急问答记录
     */
    List<HealthQa> selectEmergencyQuestions(@Param("elderId") Long elderId);
}