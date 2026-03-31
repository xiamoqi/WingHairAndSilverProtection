package com.yifasilverguard.utils;



import cn.hutool.core.date.LocalDateTimeUtil;

import java.time.LocalDateTime;

/**
 * 时间工具类
 * **/
public class TimeUtil {

    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    public static String toString(LocalDateTime localDateTime) {
        return LocalDateTimeUtil.format(localDateTime, DEFAULT_DATE_TIME_FORMAT);
    }

    public static String nowDateTimeStr() {
        return LocalDateTimeUtil.format(LocalDateTime.now(), DEFAULT_DATE_TIME_FORMAT);
    }

    public static String nowTimeStr() {
        return LocalDateTimeUtil.format(LocalDateTime.now(), DEFAULT_TIME_FORMAT);
    }

    public static String nowDateStr() {
        return LocalDateTimeUtil.format(LocalDateTime.now(), DEFAULT_DATE_FORMAT);
    }

}
