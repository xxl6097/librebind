/*
 * -----------------------------------------------------------------
 * Copyright ©2014 clife - 和而泰家居在线网络科技有限公司
 * Shenzhen H&T Intelligent Control Co.,Ltd.
 * -----------------------------------------------------------------
 *
 * File: OnTcpListener.java
 * Create: 2015/11/12 11:22
 */
package com.het.librebind.callback;

/**
 * Created by Android Studio.
 * Author: UUXIA
 * Date: 2015-11-12 11:22
 * Description:
 */
public interface OnTcpListener {
    //拒绝连接,基本是socketserver没启动
    int ECONN_REFUSED = 0x0100;
    //发送的时候服务器关闭
    int CHANNEL_CLOSED = 0x0200;
    //接收数据异常
    int READ_EXCEPTION = 0x0300;
    //写出数据异常
    int WRITE_EXCEPTION = 0x0400;

    void messageReceived(byte[] recv);
    void exceptionCaught(int id, String error);
}
