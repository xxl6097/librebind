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
     * ������ʱʱ��
     */
    private final static long keepaliveTimeout = 3 * 1000;

    public PacketBuffer createHeartBeatData() {
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
            if (tcpSocket == null) {
                tcpSocket = new TcpSocket(new OnTcpListener() {
                    @Override
                    public void messageReceived(byte[] recv) {
                        parseJson(recv);
                    }

                    @Override
                    public void exceptionCaught(int id, String error) {
                        System.out.println(Thread.currentThread().getName() + "#####################" + error + " " + id);
//                        onExceptionCaught(id, error);
                    }
                });
            }
            tcpSocket.open(device.getDeviceIp(), port);
        }
    }

    private void reConnect() {
        if (tcpSocket == null) {
            createSocket();
        } else {
            tcpSocket.open();
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
            onRecevie(basicModel.getCmd(), ison);
            switch (basicModel.getCmd()) {
                //�豸������
                case CMD.HET_DEVICE_HEARTBEAT:
                    break;
                default:
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
            if (tcpSocket == null) {
                createSocket();
            }
            if (dataQueue.size() <= 0) {
                if (tcpSocket != null) {
                    try {
                        TcpPacket tcpPacket = new TcpPacket();
                        tcpPacket.pack(curcentHeartBeatPacket.getData());
                        tcpSocket.send(tcpPacket);
                        curcentHeartBeatPacket = createHeartBeatData();
                    } catch (ClosedChannelException e) {
                        e.printStackTrace();
                        onDisconnect(tcpSocket, device, e.getMessage());
                        reConnect();
                    }
                }
            }
            long interval = System.currentTimeMillis() - curcentHeartBeatPacket.getTimestamp();
            if (interval > keepaliveTimeout) {
                //��������ʱ�����豸�����ѶϿ��������������豸
                TcpPacket tcpPacket = new TcpPacket();
                tcpPacket.pack(curcentHeartBeatPacket.getData());
                try {
                    tcpSocket.send(tcpPacket);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                    onDisconnect(tcpSocket, device, e.getMessage());
                }
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
                        //������Ϊ�գ����߳������ڴ˴�
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
        if (tcpPacket == null) {
            createSocket();
        }
        if (tcpSocket != null) {
            try {
                tcpSocket.send(tcpPacket);
            } catch (ClosedChannelException e) {
                e.printStackTrace();
                onDisconnect(tcpSocket, device, e.getMessage());
            }
        }
    }


    public void release(){
        runnable = false;
        if (tcpSocket != null)
            tcpSocket.close();
    }


    public abstract void onDisconnect(Object instance, DeviceModel device, String error);

    public abstract void onRecevie(int cmd, Object value);
}
