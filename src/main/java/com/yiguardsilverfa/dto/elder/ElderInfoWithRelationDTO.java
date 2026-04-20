package com.yiguardsilverfa.dto.elder;

import com.yiguardsilverfa.entity.ElderInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ElderInfoWithRelationDTO extends ElderInfo {
    private String relation; // 家属与老人的关系
}
