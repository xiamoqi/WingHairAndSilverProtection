package com.yiguardsilverfa.vo.login;

import com.yiguardsilverfa.constant.LoginConstant;
import lombok.Data;

@Data
public class LoginVO {
    Long id;
    String username;
    String email;
    String nickname;
    Integer role;
    String avatar;
    String token;
    String tokenExpire = LoginConstant.TOKEN_EXPIRE_TIME;
}