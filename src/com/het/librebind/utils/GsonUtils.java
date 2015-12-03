package com.het.librebind.utils;

import com.google.gson.Gson;

/**
 * Created by Android Studio.
 * Author: uuxia
 * Date: 2015-12-01 19:05
 * Description:
 */
/*
 * -----------------------------------------------------------------
 * Copyright ?2014 clife - 和而泰家居在线网络科技有限公司
 * Shenzhen H&T Intelligent Control Co.,Ltd.
 * -----------------------------------------------------------------
 *
 * File: GsonUtils.java
 * Create: 2015/12/1 19:05
 */
public class GsonUtils {
    private static Gson gson = new Gson();

    public static Object parse(String json,Class<?> clasz){
        Object obj = gson.fromJson(json, clasz);
        return obj;
    }

    public static String pack(Object obj){
        return gson.toJson(obj);
    }
}
