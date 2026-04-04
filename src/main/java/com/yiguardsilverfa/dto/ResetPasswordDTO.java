package com.yiguardsilverfa.dto;

import lombok.Data;

@Data
public class ResetPasswordDTO {
    String email;
    String newPassword;
    String verifyCode;
}