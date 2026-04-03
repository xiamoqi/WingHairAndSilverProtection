package com.yifasilverguard.service.login;

import com.yifasilverguard.dto.login.*;
import com.yifasilverguard.dto.user.UpdateUserInfoDTO;
import com.yifasilverguard.vo.login.LoginVO;
import com.yifasilverguard.vo.login.UserDetail;

public interface LoginService {

    String sendCode(CodeDTO codeDTO);

    void sendSmsCode(SendSmsCodeDTO sendSmsCodeDTO);

    UserDetail register(RegisterDTO registerDTO);

    LoginVO login(LoginDTO loginDTO);

    UserDetail getUserDetailById(Long userId);

    boolean changePassword(ChangePasswordDTO changePasswordDTO, Long userId);

    boolean resetPassword(ResetPasswordDTO resetPasswordDTO);

    // 添加绑定手机号方法
    boolean bindPhone(BindPhoneDTO bindPhoneDTO, Long userId);

    // 添加退出登录方法
    void logout(String jwt);

    // 修改个人信息
    void updateUserInfo(Long userId,UpdateUserInfoDTO updateUserInfoDTO);

    //用户注销（status=0）同时清除该用户关联的家属绑定关系
    void cancelUser(Long userId);
}