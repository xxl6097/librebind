/*
 * -----------------------------------------------------------------
 * Copyright (C) 2012-2013, by Het, ShenZhen, All rights reserved.
 * -----------------------------------------------------------------
 *
 * File: ByteUtils.java
 * Author: clark
 * Version: 1.0
 * Create: 2013-11-11
 *
 * Changes (from 2013-11-11)
 * -----------------------------------------------------------------
 * 2013-11-11 : 创建 ByteUtils.java (clark);
 * -----------------------------------------------------------------
 */
package com.het.librebind.utils;


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @ClassName: ByteUtils
 * @Description: 字节通用�?
 * @Author: clark
 * @Create: 2013-11-11
 */

public final class ByteUtils {

    private static byte HEAD = (byte) 0xF2;
    // 当前BUFFER 的总数byte,（位置指引）
    private static int currentSizeNew = 0;
    // 缓冲BUFF
    private static byte[] cashBufferNew = new byte[4096];

    /**
     * 私有的构造方�?
     */
    private ByteUtils() {
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。
     */
    public static int bytesToInt(byte[] src, int offset) {
        int value = -1;
        value = (int) (((src[offset] & 0xFF) << 24)
                | ((src[offset + 1] & 0xFF) << 16)
                | ((src[offset + 2] & 0xFF) << 8)
                | (src[offset + 3] & 0xFF));
        return value;
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * @param c
     * @return
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static byte[] short2bytes(short sht) {
        byte[] sb = new byte[2];
        for (int i = 0; i < sb.length; i++) {
            sb[i] = (byte) (sht >> (i * 8) & 0xFF);
        }
        return sb;
    }

    public static void putShort(short sht, byte[] sb, int index) {
        for (int i = 0; i < 2; i++) {
            sb[1 - i + index] = (byte) (sht >> (i * 8) & 0xFF);
        }
    }

    private static byte[] shortToByteArray(short s) {
        byte[] shortBuf = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = (shortBuf.length - 1 - i) * 8;
            shortBuf[i] = (byte) ((s >>> offset) & 0xff);
        }
        return shortBuf;
    }




    public static boolean checkCRC16(byte[] data) {
        boolean isRight = false;
        if (data != null && data.length > 0) {
            int newByteLen = data.length - 3;
            if (newByteLen > 0) {
                byte[] tmp = new byte[newByteLen];
                System.arraycopy(data, 1, tmp, 0, newByteLen);
                byte[] crcKey = ByteUtils.CRC16Calc(tmp, newByteLen);
                if (crcKey.length == 2) {
                    if (data[data.length - 2] == crcKey[0] && data[data.length - 1] == crcKey[1]) {
                        isRight = true;
                    }
                }
            }
        }
        return isRight;
    }

    /**
     * 判断字符串是否为空或空字�?
     *
     * @param strSource 源字符串
     * @return true表示为空，false表示不为�?
     */
    public static boolean isNull(final String strSource) {
        return strSource == null || "".equals(strSource.trim());
    }

    /**
     * Mac地址转换
     *
     * @param resBytes
     * @return
     */
    public static String byteToMac(byte[] resBytes) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < resBytes.length; i++) {
            String hex = Integer.toHexString(resBytes[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            buffer.append(hex.toUpperCase());
        }
        return buffer.toString();
    }

    /**
     * CRC16/X25校验
     *
     * @param data
     * @param length
     * @return
     */
    public static byte[] CRC16Calc(byte[] data, int length) {
        int j = 0;
        int crc16 = 0x0000FFFF;
        for (int i = 0; i < length; i++) {
            crc16 ^= data[i] & 0x000000FF;
            for (j = 0; j < 8; j++) {
                int flags = crc16 & 0x00000001;
                if (flags != 0) {
                    crc16 = (crc16 >> 1) ^ 0x8408;
                } else {
                    crc16 >>= 0x01;
                }
            }
        }
        int ret = ~crc16 & 0x0000FFFF;
        byte[] crc = new byte[2];
        crc[1] = (byte) (ret & 0x000000FF);
        crc[0] = (byte) ((ret >> 8) & 0x000000FF);
//        Logc.i(Integer.toBinaryString(crc[0]));
//        Logc.i(Integer.toBinaryString(crc[1]));
//        Logc.i(Integer.toBinaryString(ret));
//        Logc.i("CRC16:"+Integer.toHexString(ret));
        return crc;
    }

    /**
     * CRC16/X25校验
     *
     * @param data
     * @param length
     * @return
     */
    public static int CRC(byte[] data, int length) {
        int j = 0;
        int crc16 = 0x0000FFFF;
        for (int i = 0; i < length; i++) {
            crc16 ^= data[i] & 0x000000FF;
            for (j = 0; j < 8; j++) {
                int flags = crc16 & 0x00000001;
                if (flags != 0) {
                    crc16 = (crc16 >> 1) ^ 0x8408;
                } else {
                    crc16 >>= 0x01;
                }
            }
        }
        int ret = ~crc16 & 0x0000FFFF;
        return ret;
    }

    /**
     * 将byte转换为一个长度为8的byte数组，数组每个值代表bit
     */
    public static byte[] getByteBit(byte b) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte) (b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }

    /**
     * 把byte转为字符串的bit
     */
    public static String byteToBit(byte b) {
        return ""
                + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1)
                + (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1)
                + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1)
                + (byte) ((b >> 1) & 0x1) + (byte) ((b >> 0) & 0x1);
    }

    public static int getCommandNew(byte[] data) {
//        if (buf != null && buf.capacity() > 10) {
//            return buf.getShort(9);
//        }
        if (data != null && data.length > 5) {
            int dataLen = getDataLength(data[3],
                    data[4]);
            return dataLen;
        }
        return -1;
    }

    public static int getCommandNew(byte[] data, int index) {
        if (data != null && data.length > index) {
            int dataLen = getDataLength(data[index],
                    data[index + 1]);
            return dataLen;
        }
        return -1;
    }

    public static int getCommandForOpen(byte[] data) {
        int index = 31;
        if (data != null && data.length > index) {
            int dataLen = getDataLength(data[index],
                    data[index + 1]);
            return dataLen;
        }
        return -1;
    }


    public static String getCmd(byte[] data) {
        if (data != null && data.length >= 5) {
            byte[] macBytes = new byte[2];
            macBytes[0] = data[3];
            macBytes[1] = data[4];
            return byteToMac(macBytes);
        }
        return null;
    }

    public static String getCmdForOPen(byte[] data) {
        int index = 31;
        if (data != null && data.length >= index) {
            byte[] macBytes = new byte[2];
            macBytes[0] = data[index];
            macBytes[1] = data[index + 1];
            return byteToMac(macBytes);
        }
        return null;
    }

    public static String getCmd(byte[] data, int index) {
        if (data != null && data.length >= index) {
            byte[] macBytes = new byte[2];
            macBytes[0] = data[index];
            macBytes[1] = data[index + 1];
            return byteToMac(macBytes);
        }
        return null;
    }

    public static String getMacAddr(byte[] data, int index) {
        if (data != null && data.length > index) {
            byte[] macBytes = new byte[6];
            System.arraycopy(data, index, macBytes, 0, 6);
            return byteToMac(macBytes);
        }
        return null;
    }

    public static String getMacAddr(byte[] data) {
        if (data != null && data.length > 11) {
            byte[] macBytes = new byte[6];
            System.arraycopy(data, 5, macBytes, 0, 6);
            return byteToMac(macBytes);
        }
        return null;
    }

    public static int getTypeNew(ByteBuffer buf) {
        int type = -1;
        if (buf != null && buf.capacity() > 11) {
            type = buf.get(11);
        }
        return type;
    }

    public static String toHexString(byte[] b) {
        StringBuffer buffer = new StringBuffer();
        if (b != null)
            for (int i = 0; i < b.length; ++i) {
                String s = Integer.toHexString(b[i] & 0xFF);
                if (s.length() == 1) {
                    s = "0" + s;
                }
                buffer.append(s + " ");
            }
        return buffer.toString();
    }

    /**
     * 2字节转int
     *
     * @param byte1
     * @param byte2
     * @return
     */
    public static int getDataLength(byte byte1, byte byte2) {
        int len16 = 0;
        String hex = Integer.toHexString(byte1 & 0xFF);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        len16 = Integer.valueOf(hex, 16);
        int len17 = 0;
        String hex1 = Integer.toHexString(byte2 & 0xFF);
        if (hex1.length() == 1) {
            hex1 = '0' + hex1;
        }
        len17 = Integer.valueOf(hex1, 16);
        int pktLen = len16 * 256 + len17;
        return pktLen;
    }

    public static byte getProtocolVersion(byte[] data) {
        if (data != null && data[0] == (byte) 0xF2) {
            return data[1];
        }
        return -1;
    }

    public static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    public static byte[] short2bytes(int intvalue) {
        byte byte1 = (byte) (intvalue & 0xFF);
        byte byte2 = (byte) (intvalue >>> 8 & 0xFF);
        byte[] port = new byte[]{byte2, byte1};
        return port;
    }

    public static byte[] getBodyBytes(String ip, String port, byte[] key, byte[] ips) throws NumberFormatException, IOException {

        String[] ipArr = ip.split("\\.");
        byte[] ipByte = new byte[4];
        ipByte[0] = (byte) (Integer.parseInt(ipArr[0]) & 0xFF);
        ipByte[1] = (byte) (Integer.parseInt(ipArr[1]) & 0xFF);
        ipByte[2] = (byte) (Integer.parseInt(ipArr[2]) & 0xFF);
        ipByte[3] = (byte) (Integer.parseInt(ipArr[3]) & 0xFF);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.write(ipByte);
        if (!isNum(port))
            throw new NumberFormatException("port is not number...");
        byte[] portByte = short2bytes(Integer.parseInt(port));
        dos.write(portByte);
//        dos.writeByte(key.getBytes().length);
        dos.write(key);
        if (ips != null && ips.length > 0 && dos != null) {
            dos.write(ips);
        }
        byte[] bs = baos.toByteArray();
        baos.close();
        dos.close();

        return bs;
    }

    public static byte[] getBodyBytesForOpen(String ip, String port, byte[] key) throws NumberFormatException, IOException {

        String[] ipArr = ip.split("\\.");
        byte[] ipByte = new byte[4];
        ipByte[0] = (byte) (Integer.parseInt(ipArr[0]) & 0xFF);
        ipByte[1] = (byte) (Integer.parseInt(ipArr[1]) & 0xFF);
        ipByte[2] = (byte) (Integer.parseInt(ipArr[2]) & 0xFF);
        ipByte[3] = (byte) (Integer.parseInt(ipArr[3]) & 0xFF);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.write(key);
        dos.write(ipByte);
        if (!isNum(port))
            throw new NumberFormatException("port is not number...");
        byte[] portByte = short2bytes(Integer.parseInt(port));
        dos.write(portByte);
//        dos.writeByte(key.getBytes().length);
        byte[] bs = baos.toByteArray();
        baos.close();
        dos.close();

        return bs;
    }


    /**
     * 判断参数是否为数�?
     *
     * @param strNum 待判断的数字参数
     * @return true表示参数为数字，false表示参数非数�?
     */
    public static boolean isNum(final String strNum) {
        return strNum.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
    }
}
