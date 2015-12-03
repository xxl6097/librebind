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
	void messageReceived(byte[] recv);

    void exceptionCaught(int id, String error);
}
