package com.yiguardsilverfa.dto.elder;

import lombok.Data;
/**
 * 添加档案的DTO
 */
@Data
public class ElderInfoAddDTO {
    /**
     * 关联的账号
     */
    private Integer age;
    private Integer gender;
    private Integer height;
    private Integer weight;
    /**
     * 既往病史
     */
    private String medicalHistory;
    /**
     * 过敏史
     */
    private String allergy;
    /**
     * 紧急联系人
     */
    private String emergencyContact;
    /**
     * 紧急联系电话
     */
    private String emergencyPhone;
    private String address;
    /**
     * 如果是家属添加档案，那就必须要选择与老人的关系
     * 子女/配偶/其他
     */
    private String relation;
    /**
     * 树莓派设备编号
     */
    private String deviceId;
}
