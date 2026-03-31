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
}