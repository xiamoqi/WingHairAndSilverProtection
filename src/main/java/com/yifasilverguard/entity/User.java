package com.yifasilverguard.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    /**
     * 用户唯一 ID，自增
     **/
    Long id;
    /**
     * 用户名（用于登录）
     **/
    String username;
    /**
     * 邮箱（用于注册和接收验证码）
     **/
    String email;
    /**
     * 登录密码，加密存储（SHA256）
     **/
    String password;
    /**
     * 用户昵称/姓名
     **/
    String nickname;
    /**
     * 手机号
     **/
    String phone;
    /**
     * 手机号是否绑定 0-未绑定 1-已绑定
     **/
    Integer phoneBound;
    /**
     * 角色 1-老人 2-家属 3-管理员
     **/
    Integer role;
    /**
     * 头像地址
     **/
    String avatar;
    /**
     * 状态 0-禁用 1-正常
     **/
    Integer status;
    /**
     * 创建时间
     **/
    LocalDateTime createTime;
    /**
     * 更新时间
     **/
    LocalDateTime updateTime;
}