package com.yifasilverguard.vo.login;

import com.yifasilverguard.constant.LoginConstant;
import lombok.Data;

@Data
public class LoginVO {
    Long id;
    String username;
    String email;          // 新增
    String nickname;
    Integer role;
    String phone;
    Integer phoneBound;    // 新增：手机绑定状态
    String avatar;
    String token;
    String tokenExpire = LoginConstant.TOKEN_EXPIRE_TIME;
}