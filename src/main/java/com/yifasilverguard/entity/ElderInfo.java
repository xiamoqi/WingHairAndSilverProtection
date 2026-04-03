package com.yifasilverguard.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ElderInfo {
    /**
     * 老人ID
     **/
    Long id;
    /**
     * 对应用户ID
     **/
    Long userId;
    /**
     * 年龄
     **/
    Integer age;
    /**
     * 性别 1-男 2-女
     **/
    Integer gender;
    /**
     * 身高cm
     **/
    Integer height;
    /**
     * 体重kg
     **/
    Integer weight;
    /**
     * 既往病史
     **/
    String medicalHistory;
    /**
     * 过敏史
     **/
    String allergy;
    /**
     * 紧急联系人
     **/
    String emergencyContact;
    /**
     * 紧急联系电话
     **/
    String emergencyPhone;
    /**
     * 家庭住址
     **/
    String address;
    /**
     * 绑定设备编号（树莓派）
     **/
    String deviceId;
    /**
     * 创建时间
     **/
    LocalDateTime createTime;
    /**
     * 更新时间
     **/
    LocalDateTime updateTime;
    private Integer status;  // 1未删除，0已删除
}