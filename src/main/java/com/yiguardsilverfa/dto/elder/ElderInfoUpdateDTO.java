package com.yiguardsilverfa.dto.elder;

import lombok.Data;

@Data
public class ElderInfoUpdateDTO {
    private Long id;                 // 档案ID
    private String name;
    private Integer age;
    private Integer gender;
    private Integer height;
    private Integer weight;
    private String medicalHistory;
    private String allergy;
    private String emergencyContact;
    private String emergencyPhone;
    private String address;
    private String deviceId;
    //家属修改档案的时候可以修改关系
    private String relation;
}
