package com.att.training.spring.boot.demo.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonUtils {
    public static String singleToDoubleQuotes(String json) {
        return json.replace('\'', '"');
    }
}
