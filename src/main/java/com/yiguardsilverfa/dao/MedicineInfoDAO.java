package com.yiguardsilverfa.dao;

import com.yiguardsilverfa.entity.MedicineInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MedicineInfoDAO {
    /**
     * 插入一条数据
     */
    int insertMedicine(MedicineInfo medicine);
    /**
     * 根据ID修改药品信息
     */
    int updateMedicineById(MedicineInfo medicine);
    /**
     * 根据药品ID删除一条数据
     */
    int softdeleteById(@Param("id") Long id);
    /**
     * 根据ID查询一条数据
     */
    MedicineInfo selectById(@Param("id") Long id);
    /**
     * 根据老人档案ID查询多条数据
     * 老人可以有多个药品信息
     */
    List<MedicineInfo> selectByElderId(@Param("elderId") Long elderId);
    /**
     * 模糊查询多条数据
     */
    List<MedicineInfo> selectByCondition(@Param("elderIds") List<Long> elderIds,
                                        @Param("medicineName") String medicineName,
                                         @Param("type") String type);

}
