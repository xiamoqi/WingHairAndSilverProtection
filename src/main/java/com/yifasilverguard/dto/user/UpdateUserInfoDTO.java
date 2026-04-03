package com.yifasilverguard.dto.user;

import lombok.Data;

@Data
public class UpdateUserInfoDTO {
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 生日
     */
    private String birthday;
    /**
     * 头像URL
     */
    private String avatar;
}
