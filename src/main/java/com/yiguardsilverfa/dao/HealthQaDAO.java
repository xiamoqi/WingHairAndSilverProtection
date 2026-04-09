package com.yiguardsilverfa.dao;

import com.yiguardsilverfa.dto.healthQa.SelectHealthQaDTO;
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
    List<SelectHealthQaDTO> selectByElderId(@Param("elderId") Long elderId);
    /**
     * 查询绑定该家属的所有老人问答记录
     */
    List<SelectHealthQaDTO> selectAllByElderId(@Param("elderIds") List<Long> elderIds);

    /**
     * 查询紧急问答记录
     */
    List<HealthQa> selectEmergencyQuestions(@Param("elderId") Long elderId);
}