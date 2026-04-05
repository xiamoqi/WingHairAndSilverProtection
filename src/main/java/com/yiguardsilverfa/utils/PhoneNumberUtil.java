package com.yiguardsilverfa.utils;

import java.util.regex.Pattern;

/**
 * 手机号校验工具类
 */
public class PhoneNumberUtil {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    public static boolean isValidPhoneNumber(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
}
