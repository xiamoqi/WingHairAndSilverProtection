package com.yiguardsilverfa.entity;

import lombok.Data;

@Data
public class SysConfig {
    private Long id;
    private String configKey;
    private String configValue;
    private String remark;
}