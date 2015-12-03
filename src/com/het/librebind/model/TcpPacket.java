/*
 * -----------------------------------------------------------------
 * Copyright ?2014 clife - 和而泰家居在线网络科技有限公司
 * Shenzhen H&T Intelligent Control Co.,Ltd.
 * -----------------------------------------------------------------
 *
 * File: TcpPacket.java
 * Create: 2015/11/12 11:20
 */
package com.het.librebind.model;


import com.het.librebind.utils.AtomicIntegerUtil;

/**
 * Created by Android Studio.
 * Author: UUXIA
 * Date: 2015-11-12 11:20
 * Description:
 */
public class TcpPacket {

	private int id = AtomicIntegerUtil.getIncrementID();
	private byte[] data;

	public int getId() {
		return id;
	}

	public void pack(String txt) {
		data = txt.getBytes();
	}
	
	public void pack(byte[] txt) {
		data = txt;
	}

	public byte[] getPacket() {
		return data;
	}
}
