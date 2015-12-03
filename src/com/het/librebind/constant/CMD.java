package com.het.librebind.constant;

/**
 * Created by Android Studio.
 * Author: uuxia
 * Date: 2015-12-01 19:51
 * Description:
 */
/*
 * -----------------------------------------------------------------
 * Copyright ?2014 clife - 和而泰家居在线网络科技有限公司
 * Shenzhen H&T Intelligent Control Co.,Ltd.
 * -----------------------------------------------------------------
 *
 * File: CMD.java
 * Create: 2015/12/1 19:51
 */
public class CMD {
//    0x0001	添加歌曲
//    0x0002	查询歌曲
//    0x0003	升级操作
//    0x0004	播放
//    0x0005	关机
//    0x0006	静音/解除静音
//    0x0007	音量+
//    0x0008	音量-
//    0x0009	切换aux状态
//    0x000a	下一曲
//    0x000b	上一曲
//    0x000c	暂停
//    0x000d	播放状态查询
//    0x000e	WIFI 无线简易连接
//    0x000f	心跳
//    0x0010	扫描局域网设备
//    0x0011	回复设备信息
    public static final int HET_ADD_SONG = 0x0001;
    public static final int HET_CHECK_SONG = 0x0002;
    public static final int HET_UPDATE = 0x0003;
    public static final int HET_PALY = 0x0004;
    public static final int HET_SHUTDOWN = 0x0005;
    public static final int HET_MUTE = 0x0006;
    public static final int HET_VOLUME_ADD = 0x0007;
    public static final int HET_VOLUME_DEL = 0x0008;
    public static final int HET_AUX = 0x0009;
    public static final int HET_NEXT = 0x000a;
    public static final int HET_PRE = 0x000b;
    public static final int HET_PAUSE = 0x000c;
    public static final int HET_STATUS = 0x000d;
    public static final int HET_BIND = 0x000e;
    public static final int HET_APP_HEARTBEAT = 0x400f;
    public static final int HET_DEVICE_HEARTBEAT = 0x000f;
    public static final int HET_SCANNING = 0x4010;
    public static final int HET_SCAN_REPIY = 0x0010;
}
