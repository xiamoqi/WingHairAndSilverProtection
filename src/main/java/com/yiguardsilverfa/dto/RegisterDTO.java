package com.yiguardsilverfa.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    /**
     * 邮箱（用于注册和接收验证码）
     */
    String email;
    /**
     * 用户名（用于登录）
     */
    String username;
    /**
     * 密码
     */
    String password;
    /**
     * 姓名/昵称
     */
    String nickname;
    /**
     * 角色 1-老人 2-家属 3-管理员
     */
    Integer role;
    /**
     * 验证码
     */
    String verifyCode;
}