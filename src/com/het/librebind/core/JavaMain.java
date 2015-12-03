/*
 * -----------------------------------------------------------------
 * Copyright ?2014 clife - �Ͷ�̩�Ҿ���������Ƽ����޹�˾
 * Shenzhen H&T Intelligent Control Co.,Ltd.
 * -----------------------------------------------------------------
 *
 * File: JavaMain.java
 * Create: 2015/11/12 15:04
 */
package com.het.librebind.core;


import com.het.librebind.callback.IRecevie;
import com.het.librebind.callback.OnTcpListener;
import com.het.librebind.core.udp.UdpManager;
import com.het.librebind.model.PacketBuffer;
import com.het.librebind.model.TcpPacket;
import com.het.librebind.utils.Logc;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Arrays;

/**
 * Created by Android Studio.
 * Author: UUXIA
 * Date: 2015-11-12 15:04
 * Description:
 */
public class JavaMain implements IRecevie {
    private static TcpSocket user = null;
    public static void main(String[] args) {
        user = new TcpSocket(socketListener);
        user.open("192.168.10.3", 6688);
        TcpPacket packet = new TcpPacket();
        try {
            user.send(packet);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }
    private static OnTcpListener socketListener = new OnTcpListener() {
        @Override
        public void messageReceived(byte[] recv) {
            if (recv == null || recv.length == 0)
                return;
            System.out.println(Arrays.toString(recv));
        }

        @Override
        public void exceptionCaught(int id, String error) {
            System.err.println("onSocketDisconnect:" + error);
        }
    };

    private UdpManager createBroadcastUdpSocket(int port){
        UdpManager udpClient = null;
        try {
            String mBraodIp = null;// = IpUtils.getBroadcastAddress(this);
            if (udpClient == null) {
                udpClient = new UdpManager(mBraodIp, port);
                udpClient.setCallback(this);
            }
            String localip = null;//IpUtils.getLocalIP(this);
            if (udpClient != null) {
                udpClient.setLocalIp(localip);
            }
            udpClient.setBroadCasetIp(mBraodIp);
            Logc.i("�ɹ�����UDP Channel..�㲥��ַ:" + mBraodIp + ":" + port + " ����IP:" + localip);

        } catch (IOException e) {
            e.printStackTrace();
            Logc.e("UDP Channel ����ʧ��.." + e.getMessage(), true);
        }

        return udpClient;
    }

    @Override
    public void onRecevie(PacketBuffer packet) {

    }
}
