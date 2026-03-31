package com.yifasilverguard.dto.login;

import lombok.Data;

@Data
public class LoginDTO {
    /**
     * 用户名（用于登录）
     */
    String username;
    /**
     * 密码
     */
    String password;
}