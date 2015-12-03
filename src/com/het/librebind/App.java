package com.het.librebind;

import android.app.Application;

import com.het.librebind.callback.IRecevie;
import com.het.librebind.core.udp.UdpManager;
import com.het.librebind.utils.Utils;

import java.net.SocketException;

/**
 * Created by Android Studio.
 * Author: uuxia
 * Date: 2015-11-30 9:47
 * Description:
 */
/*
 * -----------------------------------------------------------------
 * Copyright ?2014 clife - 和而泰家居在线网络科技有限公司
 * Shenzhen H&T Intelligent Control Co.,Ltd.
 * -----------------------------------------------------------------
 *
 * File: App.java
 * Create: 2015/11/30 9:47
 */
public class App extends Application {
    private static App instance;
    private UdpManager udpManager;
    private String broadcast;
    private int port = 43708;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static App getInstance(){
        return instance;
    }

    public void createUdpServer(IRecevie callback){
        try {
            broadcast = Utils.getBroadcast();
            udpManager = new UdpManager(broadcast,port);
            udpManager.setCallback(callback);
            udpManager.setBroadCasetIp(broadcast);
            udpManager.setLocalIp(Utils.getLocalIP(this));
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
    public void send(String json,String ip,int port){
        if (udpManager != null)
            udpManager.send(json.getBytes(),ip,port);
    }

    public void scan(String json){
        send(json,broadcast,port);
    }
}
