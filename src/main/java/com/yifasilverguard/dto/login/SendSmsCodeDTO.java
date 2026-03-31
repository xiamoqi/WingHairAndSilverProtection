package com.yifasilverguard.dto.login;

import lombok.Data;

@Data
public class SendSmsCodeDTO {
    /**
     * 手机号
     */
    private String phone;
}