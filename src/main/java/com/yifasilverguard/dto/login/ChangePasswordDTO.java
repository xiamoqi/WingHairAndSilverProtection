package com.yifasilverguard.dto.login;

import lombok.Data;


@Data
public class ChangePasswordDTO {
    String oldPassword;
    String newPassword;
    String verifyCode;
}
