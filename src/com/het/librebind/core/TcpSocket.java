/*
 * -----------------------------------------------------------------
 * Copyright ?2014 clife - �Ͷ�̩�Ҿ���������Ƽ����޹�˾
 * Shenzhen H&T Intelligent Control Co.,Ltd.
 * -----------------------------------------------------------------
 *
 * File: TcpSocket.java
 * Create: 2015/11/12 11:23
 */
package com.het.librebind.core;


import com.het.librebind.callback.OnTcpListener;
import com.het.librebind.model.TcpPacket;
import com.het.librebind.utils.Logc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Android Studio.
 * Author: UUXIA
 * Date: 2015-11-12 11:23
 * Description: tcp������
 */
public class TcpSocket {
    private final int STATE_OPEN = 1;// socket��
    private final int STATE_CLOSE = 1 << 1;// socket�ر�
    private final int STATE_CONNECT_START = 1 << 2;// ��ʼ����server
    private final int STATE_CONNECT_SUCCESS = 1 << 3;// ���ӳɹ�
    private final int STATE_CONNECT_FAILED = 1 << 4;// ����ʧ��
    private final int STATE_CONNECT_WAIT = 1 << 5;// �ȴ�����

    private String IP = "203.195.139.126";
    private int PORT = 8090;
    private int state = STATE_CONNECT_START;

    Selector selector;
    ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    SocketChannel socketChannel;

    private Thread conn = null;
    private Thread rec = null;

    private OnTcpListener respListener;
    private ArrayList<TcpPacket> requestQueen = new ArrayList<TcpPacket>();
    private final Object lock = new Object();
    private final String TAG = "NioClient";

    public int send(TcpPacket in) throws ClosedChannelException {
        synchronized (lock) {
            requestQueen.add(in);
//			System.out.println("requestQueen.size===" + requestQueen.size());
        }
        if (this.selector != null) {
            this.selector.wakeup();
            socketChannel.register(selector, SelectionKey.OP_WRITE);
        }
        return in.getId();
    }

    public void cancel(int reqId) {
        Iterator<TcpPacket> mIterator = requestQueen.iterator();
        while (mIterator.hasNext()) {
            TcpPacket packet = mIterator.next();
            if (packet.getId() == reqId) {
                mIterator.remove();
            }
        }
    }

    public TcpSocket(OnTcpListener respListener) {
        this.respListener = respListener;
    }

    public boolean isSocketConnected() {
        return ((state == STATE_CONNECT_SUCCESS) && (null != conn && conn.isAlive()));
    }

    public void open() {
        reconn();
    }

    public void open(String host, int port) {
        this.IP = host;
        this.PORT = port;
        reconn();
    }

    private long lastConnTime = 0;

    public synchronized void reconn() {
        if (System.currentTimeMillis() - lastConnTime < 2000) {
            return;
        }
        lastConnTime = System.currentTimeMillis();

        close();
        state = STATE_OPEN;
        conn = new Thread(new Conn());
        conn.start();
    }

    public synchronized void close() {
        try {
            if (state != STATE_CLOSE) {
                try {
                    if (null != conn && conn.isAlive()) {
                        conn.interrupt();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
//                    conn = null;
                }

                if (null != selector && selector.isOpen()) {
                    selector.close();
                }

                try {
                    if (null != rec && rec.isAlive()) {
                        rec.interrupt();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
//                    rec = null;
                }

                state = STATE_CLOSE;
            }
            requestQueen.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class Conn implements Runnable {

        public void run() {
            try {
                state = STATE_CONNECT_START;

                selector = SelectorProvider.provider().openSelector();
                socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);

                InetSocketAddress address = new InetSocketAddress(IP, PORT);
                socketChannel.connect(address);
                socketChannel.register(selector, SelectionKey.OP_CONNECT);

                while (state != STATE_CLOSE) {
                    // ��ѡ�����Ѿ�����ͨ��������һ���������1
                    int readyChannels = selector.select();
                    if (readyChannels == 0) {
                        continue;
                    }
                    Iterator<SelectionKey> selectedKeys = selector
                            .selectedKeys().iterator();
                    while (selectedKeys.hasNext()) {
                        SelectionKey key = selectedKeys.next();
                        boolean isConnectable = key.isConnectable();
                        boolean isReadable = key.isReadable();
                        boolean isWriteable = key.isWritable();
                        boolean isAccepetable = key.isAcceptable();
                        boolean isValid = key.isValid();
                        Logc.i("=====connect=" + isConnectable + " read=" + isReadable + " " +
                                "write=" + isWriteable + " accpet=" + isAccepetable + " valid=" + isValid);
                        if (!isValid) {
                            continue;
                        }
                        if (isConnectable) {
                            finishConnection(key);
                        } else if (isReadable) {
                            read(key);
                        } else if (isWriteable) {
                            write(key);
                        }
                        selectedKeys.remove();
                    }
                    synchronized (lock) {
                        if (requestQueen.size() > 0) {
                            SelectionKey key = socketChannel.keyFor(selector);
                            if (key != null) {
                                key.interestOps(SelectionKey.OP_WRITE);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (respListener != null){
                    respListener.exceptionCaught(0,e.getMessage());
                }
            } finally {
                if (null != socketChannel) {
                    SelectionKey key = socketChannel.keyFor(selector);
                    if (key != null) {
                        key.cancel();
                    }
                    try {
                        if (socketChannel != null) {
                            socketChannel.close();
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            // System.out.println("Conn :End");
        }

        private boolean finishConnection(SelectionKey key) throws IOException {
            boolean result = false;
            SocketChannel socketChannel = (SocketChannel) key.channel();
            // �жϴ�ͨ�����Ƿ����ڽ������Ӳ�����
            // ����׽���ͨ�������ӹ��̡�
            if (socketChannel.isConnectionPending()) {
                // ������ӵĽ�����TCP�������֣�
                result = socketChannel.finishConnect();// û�������ʱ��Ҳ����true
                if (result) {
                    key.interestOps(SelectionKey.OP_READ);
                    state = STATE_CONNECT_SUCCESS;
                }
                System.out.println("finishConnection :" + result);
            }
            return result;
        }

        private void read(SelectionKey key) throws IOException {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            readBuffer.clear();
            int numRead;
            numRead = socketChannel.read(readBuffer);
            if (numRead == -1) {
                key.channel().close();
                key.cancel();
                return;
            }
            // respListener.onSocketResponse(new String(readBuffer.array(),
            // 0,numRead));

            byte[] stores = new byte[numRead];
            System.arraycopy(readBuffer.array(), 0, stores, 0, numRead);
            respListener.messageReceived(stores);

//			key.interestOps(SelectionKey.OP_WRITE);
        }

        private void write(SelectionKey key) throws IOException {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            synchronized (lock) {
                TcpPacket item;
                Iterator<TcpPacket> iter = requestQueen.iterator();
                while (iter.hasNext()) {
                    item = iter.next();
                    ByteBuffer buf = ByteBuffer.wrap(item.getPacket());
                    socketChannel.write(buf);
                    iter.remove();
                }
                item = null;
            }
            key.interestOps(SelectionKey.OP_READ);
        }
    }
}
