package com.yiguardsilverfa.service.login;

import com.yiguardsilverfa.dto.*;
import com.yiguardsilverfa.dto.user.UpdateUserInfoDTO;
import com.yiguardsilverfa.entity.Result;
import com.yiguardsilverfa.vo.login.LoginVO;
import com.yiguardsilverfa.vo.login.UserDetail;

public interface LoginService {

    String sendCode(CodeDTO codeDTO);


    UserDetail register(RegisterDTO registerDTO);

    LoginVO login(LoginDTO loginDTO);

    UserDetail getUserDetailById(Long userId);

    boolean changePassword(ChangePasswordDTO changePasswordDTO, Long userId);

    boolean resetPassword(ResetPasswordDTO resetPasswordDTO);


    // 添加退出登录方法
    void logout(String jwt);

    // 修改个人信息
    Result<?> updateUserInfo(Long userId, UpdateUserInfoDTO updateUserInfoDTO);

    //用户注销（status=0）同时清除该用户关联的家属绑定关系
    Result<?> cancelUser(Long userId);
}