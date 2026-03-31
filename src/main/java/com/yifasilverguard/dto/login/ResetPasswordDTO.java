package com.yifasilverguard.dto.login;

import lombok.Data;

@Data
public class ResetPasswordDTO {
    String email;
    String newPassword;
    String verifyCode;
}