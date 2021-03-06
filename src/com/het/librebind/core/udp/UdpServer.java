package com.het.librebind.core.udp;


import com.het.librebind.model.PacketBuffer;
import com.het.librebind.utils.Logc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

/**
 * Created by uuxia-mac on 15/8/29.
 */
public class UdpServer extends BaseThread {
    protected DatagramPacket datagramPacket;
    protected byte[] buffer = new byte[8192];

    public UdpServer(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
        setName("UdpServer");
    }

    @Override
    public void run() {
        super.run();
        datagramPacket = new DatagramPacket(buffer, buffer.length);
        while (runnable) {
            try {
                datagramSocket.receive(datagramPacket);
                PacketBuffer packet = new PacketBuffer();
                packet.setData(datagramPacket.getData());
                packet.setLength(datagramPacket.getLength());
                packet.setPort(datagramPacket.getPort());
                packet.setIp(datagramPacket.getAddress().getHostAddress().toString());
//                Logc.i("接收队列大小->" + inQueue.size() + " " + ByteUtils.toHexString(packet.getData()));//+""+ ByteUtils.toHexString(datagramPacket.getData()));
                boolean b = inQueue.offer(packet);
                if (!b) {
                    Logc.e("this packet is loss:" + packet.toString());
                }
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                Logc.i("UDPReceiver.start.run_SocketTimeoutException" + e.getMessage());
            } catch (IOException e) {
                Logc.i("UDPReceiver.start.run_IOException" + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
