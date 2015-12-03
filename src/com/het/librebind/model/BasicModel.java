package com.het.librebind.model;

import java.io.Serializable;

/**
 * Created by Android Studio.
 * Author: uuxia
 * Date: 2015-11-30 19:51
 * Description:
 */
/*
 * -----------------------------------------------------------------
 * Copyright ?2014 clife - 和而泰家居在线网络科技有限公司
 * Shenzhen H&T Intelligent Control Co.,Ltd.
 * -----------------------------------------------------------------
 *
 * File: BasicModel.java
 * Create: 2015/11/30 19:51
 */
public class BasicModel<T> implements Serializable {
    private int cmd;
    private String code;
    private String msg;
    private T data;

    public BasicModel() {
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "BasicModel{" +
                "cmd=" + cmd +
                ", code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
