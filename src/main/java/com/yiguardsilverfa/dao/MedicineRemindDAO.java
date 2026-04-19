package com.yiguardsilverfa.dao;

import com.yiguardsilverfa.entity.MedicineRemind;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface MedicineRemindDAO {
    int insertMedicineRemind(MedicineRemind Remind);
    int updateMedicineRemind(MedicineRemind Remind);
    int deleteMedicineRemind(@Param("id") Long id);
    MedicineRemind selectById(@Param("id") Long id);
    List<MedicineRemind> selectByElderId(@Param("elderId") Long elderId);
}
