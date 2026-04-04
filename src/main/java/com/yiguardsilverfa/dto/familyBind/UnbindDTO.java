package com.yiguardsilverfa.dto.familyBind;

import lombok.Data;

/**
 * 解绑老人档案
 */
@Data
public class UnbindDTO {
    private Long elderId;    // 要解除绑定的老人ID
}
