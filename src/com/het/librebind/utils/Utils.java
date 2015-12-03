package com.het.librebind.utils;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.het.librebind.wifi.WifiConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by Android Studio.
 * Author: uuxia
 * Date: 2015-11-30 9:53
 * Description:
 */
/*
 * -----------------------------------------------------------------
 * Copyright ?2014 clife - 和而泰家居在线网络科技有限公司
 * Shenzhen H&T Intelligent Control Co.,Ltd.
 * -----------------------------------------------------------------
 *
 * File: Utils.java
 * Create: 2015/11/30 9:53
 */
public class Utils {
    public static void main(String[] args) throws Exception {
        long num=Math.round(Math.random() * 10000000);
        System.out.println(num);
    }

    public static String doPost(String uriApi,WifiConnection wifiConnect)
    {
//        uriApi = "http://apistore.baidu.com/microservice/cityinfo";//Post方式没有参数在这里
        String result = "";
        HttpPost httpRequst = new HttpPost(uriApi);//创建HttpPost对象

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("SSID", wifiConnect.getMainSSID()));
        params.add(new BasicNameValuePair("Passphrase", wifiConnect.getMainSSIDPwd()));
        params.add(new BasicNameValuePair("Security", wifiConnect.getMainSSIDSec()));
        params.add(new BasicNameValuePair("Devicename", wifiConnect.getDeviceName()));

//        params.add(new BasicNameValuePair("cityname", "%E5%8C%97%E4%BA%AC"));

        try {
            HttpEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
            httpRequst.setEntity(entity);
            HttpClient client = new DefaultHttpClient();
            int timeout = 5000;
            // 请求超时
            client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
            // 读取超时
            client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);

            HttpResponse httpResponse = client.execute(httpRequst);
            if(httpResponse.getStatusLine().getStatusCode() == 200)
            {
                HttpEntity httpEntity = httpResponse.getEntity();
                result = EntityUtils.toString(httpEntity);//取出应答字符串
                result+=" http.code="+httpResponse.getStatusLine().getStatusCode();
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            result = e.getMessage().toString();
        }
        catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            result = e.getMessage().toString();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            result = e.getMessage().toString();
        }
        return result;
    }


    /**
     * 获取本机
     *
     * @param ctx
     * @return
     */
    public static String getLocalIP(Context ctx) {
        WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo di = wm.getDhcpInfo();
        long ip = di.ipAddress;
        StringBuffer sb = new StringBuffer();
        sb.append(String.valueOf((int) (ip & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 8) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 16) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 24) & 0xff)));
        return sb.toString();
    }


    public static String getBroadcast() throws SocketException {
        System.setProperty("java.net.preferIPv4Stack", "true");
        for (Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces(); niEnum.hasMoreElements(); ) {
            NetworkInterface ni = niEnum.nextElement();
            if (!ni.isLoopback()) {
                for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {
//                    System.out.println("MYACTIVITY "+ ni.getName() +" "+ interfaceAddress.getBroadcast());
                    if (interfaceAddress.getBroadcast() != null && (ni.getName().contains("wlan") || ni.getName().contains("eth1"))) {
                        String broad = interfaceAddress.getBroadcast().toString().substring(1);
                        Log.i("MYACTIVITY", ni.getName() + " 广播地址==" + broad);
                        return broad;
                    }
                }
            }
        }
        return null;
    }


    public static NetworkInterface getActiveNetworkInterface() {

        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return null;
        }

        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = iface.getInetAddresses();


            /* Check if we have a non-local address. If so, this is the active
             * interface.
             *
             * This isn't a perfect heuristic: I have devices which this will
             * still detect the wrong interface on, but it will handle the
             * common cases of wifi-only and Ethernet-only.
             */
            if (iface.getName().startsWith("w")) {
                //this is a perfect hack for getting wifi alone

                while (inetAddresses.hasMoreElements()) {
                    InetAddress addr = inetAddresses.nextElement();

                    if (!(addr.isLoopbackAddress() || addr.isLinkLocalAddress())) {
                        Log.d("LSSDP", "DisplayName" + iface.getDisplayName() + "Name" + iface.getName());

                        return iface;
                    }
                }
            }
        }

        return null;
    }

    public static String getconnectedSSIDname(Context context) {
        WifiManager wifiManager;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();
        Log.d("CreateNewScene", "getconnectedSSIDname wifiInfo = " + wifiInfo.toString());
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        Log.d("SacListActivity", "Connected SSID" + ssid);
        return ssid;
    }
}
