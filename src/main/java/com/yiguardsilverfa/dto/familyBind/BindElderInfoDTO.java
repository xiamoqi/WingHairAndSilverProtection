package com.yiguardsilverfa.dto.familyBind;

import lombok.Data;

/**
 * 获取家属绑定老人列表信息
 */
@Data
public class BindElderInfoDTO {
    private Long elderInfoId;   // 档案ID (elder_info.id)
    private Long userId;        // 老人账号ID (可为空)
    private String username;    // 老人用户名

}
