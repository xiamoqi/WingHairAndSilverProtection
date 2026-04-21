package com.yiguardsilverfa.dao;

import com.yiguardsilverfa.entity.WarningEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface WarningEventDAO {
    int insert(WarningEvent warningEvent);
    List<WarningEvent> selectByElderId(@Param("elderId") Long elderId);
    List<WarningEvent> selectUnhandledByElderId(@Param("elderId") Long elderId);
    WarningEvent updateHandleStatus(@Param("id") Long id, @Param("handleStatus") Integer handleStatus, @Param("handleUser") Long handleUser);
}
