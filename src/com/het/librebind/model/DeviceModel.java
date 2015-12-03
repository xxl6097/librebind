package com.het.librebind.model;

import java.io.Serializable;

/**
 * Created by Android Studio.
 * Author: uuxia
 * Date: 2015-12-01 19:27
 * Description:
 */
/*
 * -----------------------------------------------------------------
 * Copyright ?2014 clife - 和而泰家居在线网络科技有限公司
 * Shenzhen H&T Intelligent Control Co.,Ltd.
 * -----------------------------------------------------------------
 *
 * File: DeviceModel.java
 * Create: 2015/12/1 19:27
 */
public class DeviceModel implements Serializable {

    /**
     * deviceIp : 192.168.10.111
     * deviceMac : acc23442212
     * deviceType : 1
     * deviceSubType : 2
     * brandId : 18029364
     */

    private String deviceId;
    private String deviceIp;
    private String deviceMac;
    private String deviceType;
    private String deviceSubType;
    private String brandId;

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setDeviceSubType(String deviceSubType) {
        this.deviceSubType = deviceSubType;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getDeviceIp() {
        return deviceIp;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceSubType() {
        return deviceSubType;
    }

    public String getBrandId() {
        return brandId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return "DeviceModel{" +
                "deviceId='" + deviceId + '\'' +
                ", deviceIp='" + deviceIp + '\'' +
                ", deviceMac='" + deviceMac + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", deviceSubType='" + deviceSubType + '\'' +
                ", brandId='" + brandId + '\'' +
                '}';
    }
}
