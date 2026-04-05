package com.yiguardsilverfa.dto.familyBind;

import lombok.Data;

/**
 * 绑定老人账号
 */
@Data
public class BindElderAccountDTO {
    private Long elderInfoId;   // 档案ID
    private Long elderuserId;        // 老人账号ID
}
