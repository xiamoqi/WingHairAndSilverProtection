package com.yifasilverguard.dto.login;

import lombok.Data;

@Data
public class BindPhoneDTO {
    /**
     * 手机号
     */
    private String phone;
    /**
     * 短信验证码
     */
    private String smsCode;
}