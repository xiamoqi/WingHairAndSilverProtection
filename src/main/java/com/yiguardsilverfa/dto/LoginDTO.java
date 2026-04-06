package com.yiguardsilverfa.dto;

import lombok.Data;

@Data
public class LoginDTO {
    /**
     * 用户名（用于登录）
     */
    String phone;
    /**
     * 密码
     */
    String password;
}