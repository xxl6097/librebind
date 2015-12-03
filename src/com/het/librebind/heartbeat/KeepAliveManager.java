package com.het.librebind.heartbeat;

import com.het.librebind.utils.Logc;

import java.util.Iterator;
import java.util.Vector;

/**
 * Created by UUXIA on 2015/6/15.
 * 设备心跳管理类
 */
public class KeepAliveManager{
    /**
     * 发送心跳包的间隔时间
     */
    private final static long keepalivetime = 10 * 1000;
    public static KeepAliveManager instance = null;
    static int index = 0;
//    public static ConcurrentHashMap<Integer, PacketBuffer> dataQueue = new ConcurrentHashMap<Integer, PacketBuffer>();
    //    protected static BlockingQueue<PacketBuffer> dataQueue = new LinkedBlockingQueue<PacketBuffer>();
    private static Vector<OnDeviceOnlineListener> onDeviceOnlineVector = new Vector<OnDeviceOnlineListener>();
    private static boolean working = true;

    private Thread keepAliveThread = null;

    public KeepAliveManager() {
        startKeepAlive();
    }

    public static KeepAliveManager getInstnce() {
        if (instance == null) {
            instance = new KeepAliveManager();
        }
        return instance;
    }

    public void resgisterDeviceOnlineListener(final OnDeviceOnlineListener onDeviceOnlineListener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (onDeviceOnlineListener != null) {
                    Thread.State state = keepAliveThread.getState();
                    if (state.equals(Thread.State.WAITING)) {
                        synchronized (onDeviceOnlineVector) {
                            if (!onDeviceOnlineVector.contains(onDeviceOnlineListener)) {
                                onDeviceOnlineVector.add(onDeviceOnlineListener);
                            }
                            onDeviceOnlineVector.notifyAll();
                        }
                    } else {
                        if (!onDeviceOnlineVector.contains(onDeviceOnlineListener)) {
                            onDeviceOnlineVector.add(onDeviceOnlineListener);
                        }
                    }
                    Logc.d("keepalive 注册-" + onDeviceOnlineListener.getClass().getName());
                }
            }
        }, "注册-" + (index++)).start();
    }

    /**
     * 注销监听
     *
     * @param onDeviceOnlineListener
     */
    public void unresgisterDeviceOnlineListener(final OnDeviceOnlineListener onDeviceOnlineListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (onDeviceOnlineListener != null) {
                    Thread.State state = keepAliveThread.getState();
                    if (state.equals(Thread.State.WAITING)) {
                        synchronized (onDeviceOnlineVector) {
                            if (onDeviceOnlineVector.contains(onDeviceOnlineListener)) {
                                onDeviceOnlineListener.release();
                                onDeviceOnlineVector.remove(onDeviceOnlineListener);
                            }
                            onDeviceOnlineVector.notifyAll();
                        }
                    } else {
                        if (onDeviceOnlineVector.contains(onDeviceOnlineListener)) {
                            onDeviceOnlineListener.release();
                            onDeviceOnlineVector.remove(onDeviceOnlineListener);
                        }
                    }
                    Logc.d("keepalive 销毁-" + onDeviceOnlineListener.getClass().getName());
                }
            }
        }, "销毁-" + (index++)).start();
    }


    /**
     * 事件触发
     */
    private void trigger() {
        Iterator<OnDeviceOnlineListener> it = onDeviceOnlineVector.iterator();
        while (it.hasNext()) {
            OnDeviceOnlineListener mgr = it.next();
            mgr.keepHeartBeatAlive();
        }
        onDeviceOnlineVector.notifyAll();
    }

    private void startKeepAlive() {
        if (keepAliveThread == null) {
            keepAliveThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("KeepAliveManager start...");
                    synchronized (onDeviceOnlineVector) {
                        while (working) {
                            while (onDeviceOnlineVector.size() == 0) {
                                try {
                                    onDeviceOnlineVector.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    onDeviceOnlineVector.notifyAll();
                                }
                            }
                            trigger();
                            try {
                                onDeviceOnlineVector.wait(keepalivetime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } finally {
                                onDeviceOnlineVector.notifyAll();
                            }
                        }
                    }
                }
            }, "KeepAliveManager-心跳包管理");
            keepAliveThread.start();
        }
    }

    public void close() {
        working = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (onDeviceOnlineVector) {
                    onDeviceOnlineVector.clear();
                    onDeviceOnlineVector.notifyAll();
                }
            }
        }, "keepalive.close").start();
    }

}
