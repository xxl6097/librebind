package com.het.librebind.heartbeat;

import com.google.gson.internal.LinkedTreeMap;
import com.het.librebind.callback.OnTcpListener;
import com.het.librebind.constant.CMD;
import com.het.librebind.core.TcpSocket;
import com.het.librebind.model.BasicModel;
import com.het.librebind.model.DeviceModel;
import com.het.librebind.model.PacketBuffer;
import com.het.librebind.model.TcpPacket;
import com.het.librebind.utils.GsonUtils;
import com.het.librebind.utils.Logc;

import java.io.UnsupportedEncodingException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by UUXIA on 2015/6/15.
 */
public abstract class OnDeviceOnlineListener {
    private int port = 43707;
    private DeviceModel device;
    private TcpSocket tcpSocket;
    private BlockingQueue<PacketBuffer> dataQueue = new LinkedBlockingQueue<PacketBuffer>();
    private PacketBuffer curcentHeartBeatPacket = createHeartBeatData();
    protected boolean runnable = true;
    private Thread backgroudThread;
    /**
     * 心跳超时时间
     */
    private final static long keepaliveTimeout = 3 * 1000;

    private PacketBuffer createHeartBeatData(){
        BasicModel heart = new BasicModel();
        heart.setCmd(CMD.HET_APP_HEARTBEAT);
        heart.setCode("0");
        String sendData = GsonUtils.pack(heart);
        PacketBuffer packet = new PacketBuffer();
        packet.setCommand(CMD.HET_APP_HEARTBEAT);
        packet.setData(sendData.getBytes());
        return packet;
    }

    public OnDeviceOnlineListener() {
        createSocket();
        startBackGroudThread();
    }

    private void createSocket(){
        if (device != null) {
            if (tcpSocket == null || !tcpSocket.isSocketConnected()) {
                tcpSocket = new TcpSocket(new OnTcpListener() {
                    @Override
                    public void messageReceived(byte[] recv) {
                        parseJson(recv);
                    }

                    @Override
                    public void exceptionCaught(int id, String error) {
                        System.out.println(Thread.currentThread().getName()+"#####################"+error);
                        onExceptionCaught(id, error);
                    }
                });
            }
            tcpSocket.open(device.getDeviceIp(), port);
        }
    }

    private void parseJson(byte[] data){
        BasicModel basicModel = null;
        String ison = null;
        try {
            String json = new String(data, "GBK");
            basicModel = (BasicModel) GsonUtils.parse(json, BasicModel.class);
            LinkedTreeMap map = (LinkedTreeMap) basicModel.getData();
            ison = GsonUtils.pack(map);
            System.out.println(ison);
            if (basicModel == null)
                return;
            if (ison == null)
                return;
            switch (basicModel.getCmd()) {
                //设备心跳包
                case CMD.HET_DEVICE_HEARTBEAT:
                    break;
                default:
                    onRecevie(ison);
                    break;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param dm
     */
    public OnDeviceOnlineListener(DeviceModel dm) {
        this();
        if (dm != null) {
            device = dm;
        }
    }

    public void keepHeartBeatAlive(){
        if (device != null) {
            if (dataQueue.size() <= 0) {
                if (tcpSocket != null) {
                    try {
                        TcpPacket tcpPacket = new TcpPacket();
                        tcpPacket.pack(curcentHeartBeatPacket.getData());
                        System.out.println("000000000000000000000" + tcpSocket.isSocketConnected());
                        tcpSocket.send(tcpPacket);
                        curcentHeartBeatPacket = createHeartBeatData();
                    } catch (ClosedChannelException e) {
                        e.printStackTrace();
                        onDisconnect(tcpSocket, device);
                        if (!tcpSocket.isSocketConnected()){
                            createSocket();
                        }
                    }
                }
            }
            long interval = System.currentTimeMillis() - curcentHeartBeatPacket.getTimestamp();
            if (interval > keepaliveTimeout) {
                //心跳包超时，与设备连接已断开，请重新连接设备
                onDisconnect(tcpSocket, device);
            }
        }
    }

    /**
     *
     * @param dm
     */
    public void setDevice(DeviceModel dm) {
        if (dm != null) {
            device = dm;
            createSocket();
        }
    }

    public void send(PacketBuffer packet){
        boolean b = dataQueue.offer(packet);
        if (!b) {
            Logc.e("this packet offer faile:" + packet.toString());
        }
    }

    private void startBackGroudThread(){
        backgroudThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (runnable) {
                    try {
                        //若队列为空，则线程阻塞在此处
                        PacketBuffer data = dataQueue.take();
                        write(data);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        backgroudThread.start();
    }

    private void write(PacketBuffer data){
        TcpPacket tcpPacket = new TcpPacket();
        tcpPacket.pack(data.getData());
        if (tcpSocket == null){
            createSocket();
        }
        if (tcpSocket != null) {
            try {
                tcpSocket.send(tcpPacket);
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            }
        }
    }


    public void release(){
        runnable = false;
        tcpSocket.close();
    }

    public abstract void onExceptionCaught(int id, String error);

    public abstract void onDisconnect(Object instance, DeviceModel device);

    public abstract void onRecevie(Object value);
}
