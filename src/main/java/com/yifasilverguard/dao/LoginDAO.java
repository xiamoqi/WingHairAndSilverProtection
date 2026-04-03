package com.yifasilverguard.dao;

import com.yifasilverguard.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LoginDAO {

    void insertUser(User user);

    User selectUserByUsername(String username);

    User selectUserByEmail(String email);  // 新增

    User selectUserById(Long id);

    void updateUserById(User user);

    void updateUserByUsername(User user);

    int checkUsernameExists(String username);  // 新增

    int checkEmailExists(String email);  // 新增

    void bindPhone(@Param("userId") Long userId, @Param("phone") String phone);  // 新增
    // 根据手机号查询用户
    User selectUserByPhone(@Param("phone") String phone);
    // 动态更新用户信息（仅更新传入的非空字段）
    int updateUserSelective(User user);
    //用户注销（将status字段置为0）
    int logout(@Param("userId") Long userId);
    //删除家属绑定关系
    int deleteFamilybind(@Param("userId") Long userId);

}