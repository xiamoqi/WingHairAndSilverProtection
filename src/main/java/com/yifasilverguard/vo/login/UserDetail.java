package com.yifasilverguard.vo.login;

import lombok.Data;

@Data
public class UserDetail {
    /**
     * 用户ID
     */
    Long id;
    /**
     * 账号
     */
    String username;
    /**
     * 昵称/姓名
     */
    String nickname;
    /**
     * 手机号
     */
    String phone;
    /**
     * 角色 1-老人 2-家属 3-管理员
     */
    Integer role;
    /**
     * 头像
     */
    String avatar;
    /**
     * 状态 0-禁用 1-正常
     */
    Integer status;
    /**
     * 创建时间
     */
    String createTime;
}