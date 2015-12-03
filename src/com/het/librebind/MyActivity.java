package com.het.librebind;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.internal.LinkedTreeMap;
import com.het.librebind.callback.IRecevie;
import com.het.librebind.callback.OnTcpListener;
import com.het.librebind.constant.CMD;
import com.het.librebind.core.TcpSocket;
import com.het.librebind.heartbeat.KeepAliveManager;
import com.het.librebind.heartbeat.OnDeviceOnlineListener;
import com.het.librebind.model.BasicModel;
import com.het.librebind.model.DeviceModel;
import com.het.librebind.model.PacketBuffer;
import com.het.librebind.model.TcpPacket;
import com.het.librebind.utils.GsonUtils;
import com.het.librebind.utils.Logc;
import com.het.librebind.utils.Utils;
import com.het.librebind.wifi.EnableWifi;
import com.het.librebind.wifi.WifiConnection;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MyActivity extends Activity {
    private WifiConnection wifiConnect = WifiConnection.getInstance();
    private WifiManager mWifiManager;
    private WifiReceiver receiverWifi;
    private ListView apListView;
    ArrayAdapter<String> adpter;
    public ProgressDialog mProgressDialog;
    private boolean isLibreConnected = true;

    private HashMap<String,DeviceModel> discoverDeviceSet = new HashMap<>();

    private List<DeviceModel> mData = new ArrayList<>();
    private Adpter deviceAdpter;
    private boolean isDiscoverDevice = false;

    final int HTTP_POST_RESPONSE = 0x0011;
    final int CONNECT_ORIGINAL_SSID = 0x00aa;

    private OnDeviceOnlineListener deviceOnlineListener = new OnDeviceOnlineListener() {
        @Override
        public void onExceptionCaught(int id, String error) {
            System.out.println(id+"---------exceptionCaught-------"+error);
        }

        @Override
        public void onDisconnect(Object instance, DeviceModel device) {
            TcpSocket tcp = (TcpSocket) instance;
            System.out.println(tcp.isSocketConnected()+"--------onDisconnect--------"+device);
        }

        @Override
        public void onRecevie(Object value) {
            System.out.println("--------onRecevie--------"+value.toString());
        }
    };



    private Runnable mLoadingRunnable = new Runnable() {
        @Override
        public void run() {
            init();
        }
    };
    private TextView text;

    private void tips(final  String tip){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyActivity.this, tip, Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        discoverDeviceSet.clear();
        initView();
        //优化的DelayLoad
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                wifihandler.post(mLoadingRunnable);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KeepAliveManager.getInstnce().unresgisterDeviceOnlineListener(deviceOnlineListener);
    }

    public void closeLoader() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    Logc.d("===progress Dialog Closed");
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.dismiss();
                    mProgressDialog.cancel();
                }
            }
        });


    }

    public void showLoader(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog == null) {
                    mProgressDialog = ProgressDialog.show(MyActivity.this, "Notice", "Connecting To " + msg + "...", true, true, null);
                }
                mProgressDialog.setCancelable(false);
                Logc.d("===showLoader isShowing:" + mProgressDialog.isShowing() + " finish:" + MyActivity.this.isFinishing());
                if (!mProgressDialog.isShowing()) {
                    if (!(MyActivity.this.isFinishing())) {
                        mProgressDialog = ProgressDialog.show(MyActivity.this, "Notice", "Connecting To  " + msg + "...", true, true, null);
                    } else {
                        mProgressDialog.setTitle(msg);
                    }
                } else {
                    mProgressDialog.setTitle(msg);
                }
            }
        });

    }

    private void initView(){
        text = (TextView) findViewById(R.id.text);
        apListView = (ListView) findViewById(R.id.aplist);
        adpter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, wifiConnect.mSACDevicesList);
        isDiscoverDevice = false;
        deviceAdpter = new Adpter(this,mData);
        apListView.setAdapter(adpter);
        apListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isDiscoverDevice) {
                    DeviceModel dm = mData.get(position);
                    tips(dm.toString());
                    deviceOnlineListener.setDevice(dm);
                    return;
                }
                if (isLibreConnected) {
                    tips(wifiConnect.getAllSacDeviceList().get(position));
                    if (!wifiConnect.getSacDevice(position).contains(getResources().getString(R.string.title_no_sac_device))) {
                        mWifiManager.disconnect();
//                        showLoader(wifiConnect.getSacDevice(position));
                        wifiConnect.setMainSSIDDetails(Utils.getconnectedSSIDname(MyActivity.this), "", "");
//                        handleScanResultsAvailable();

                        Logc.d("===onItemClick"+wifiConnect.getSacDevice(position));
                        connecToSSId(wifiConnect.getSacDevice(position), "", true);
                    }
                } else {
                    tips("please input ssid's:" + wifiConnect.getAllSacDeviceList().get(position) + " and password");
                    inputPassword(wifiConnect.getAllSacDeviceList().get(position));
                }
            }
        });
    }

    private void inputPassword(String ssid){
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View alertlayout = layoutInflater.inflate(R.layout.alertlayout, null);
        final EditText devicename = (EditText) alertlayout.findViewById(R.id.devicename);
        final TextView ssidname = (TextView) alertlayout.findViewById(R.id.ssid);
        long num=Math.round(Math.random() * 100000);
        devicename.setText("HeT-"+num);
        ssidname.setText(ssid);
        final EditText password = (EditText) alertlayout.findViewById(R.id.password);
        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setMessage("please input " + ssid + "'s password ");
        dlg.setView(alertlayout);
        dlg.setPositiveButton("doPost", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = devicename.getText().toString();
                String pass = password.getText().toString();
                tips("pass=" + pass);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        showLoader(ssid +" 正在提交POST请求...");
                        sendPostDataToInternet(ssid, pass, name,true);
                    }
                }).start();
            }
        });
        dlg.setNegativeButton("HttpURLConnection", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = devicename.getText().toString();
                String pass = password.getText().toString();
                tips("pass=" + pass);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        showLoader(ssid+" 正在提交POST请求...");
                        sendPostDataToInternet(ssid, pass, name,false);
                    }
                }).start();
            }
        });
        dlg.show();
    }

    private String sendPostDataToInternet(String ssid,String pass,String name,boolean doPost)
    {
        wifiConnect.setMainSSID(ssid);
        wifiConnect.setMainSSIDPwd(pass);
        wifiConnect.setDeviceName(name);
        //                 http://192.168.43.1:80/goform/HandleSACConfiguration
        String mURLLink = "http://192.168.43.1:80/goform/HandleSACConfiguration";
        Message msg = Message.obtain();

        if (doPost) {
            String ret = Utils.doPost(mURLLink, wifiConnect);
            if (ret != null) {
                msg.what = HTTP_POST_RESPONSE;
                msg.arg1 = -200;
                msg.obj = ret;
                wifihandler.sendMessage(msg);
            }
            closeLoader();
            return ret;
        }


        try{
            String sDeviceName = wifiConnect.getssidDeviceNameSAC(ssid);//deviceName.getText().toString(); //wifiConnect.getssidDeviceNameSAC(strTxt);
            URL url = new URL(mURLLink);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            Map<String,Object> params = new LinkedHashMap<>();
            params.put("SSID", wifiConnect.getMainSSID());
            params.put("Passphrase", wifiConnect.getMainSSIDPwd());
            params.put("Security", wifiConnect.getMainSSIDSec());
            params.put("Devicename", wifiConnect.getDeviceName());

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String,Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }


            String urlParameters = postData.toString();
            System.out.println("================http parameters:" + urlParameters);
            conn.setDoOutput(true);

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(urlParameters);
            writer.flush();


            writer.close();
            wifiConnect.setmSACDevicePostDone(true);
            int responseCode = conn.getResponseCode();

            msg.what = HTTP_POST_RESPONSE;
            msg.arg1 = responseCode;
            msg.obj = responseCode;
            wifihandler.sendMessage(msg);

//            if (responseCode == 200){
//                wifihandler.sendEmptyMessage(HTTP_POST_RESPONSE);
//            }else{
//                msg.what = POST_FAILED;
//                msg.obj = responseCode;
//                wifihandler.sendMessage(msg);
//            }
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("================http rsp:" + responseCode);
            System.out.println("Post parameters : " + urlParameters);
            System.out.println("Response Code : " + responseCode);
            wifiConnect.setmSACDevicePostDone(true);
            //mHandler.obtainMessage(Constants.HTTP_POST_DONE_SUCCESSFULLY, "DONE").sendToTarget();
        }catch(Exception e){
            msg.what = HTTP_POST_RESPONSE;
            msg.obj = e.getMessage();
            wifihandler.sendMessage(msg);
            wifiConnect.setmSACDevicePostDone(false);
            System.out.println("================http parameters:" + e.getMessage());
            e.printStackTrace();
        }finally {
            closeLoader();
        }
        return null;
    }



    public void connecToSSId(final String mNetworkSsidToConnect, String passkey, final boolean bSACDEVICE) {
        showLoader(mNetworkSsidToConnect);
//        if (!bSACDEVICE) {/* If Its Not SAC Device We are showing Loader and Disconnect From Previous Network Connect*/
//            showLoader(mNetworkSsidToConnect);
//            //mWifiManager.disconnect();
//        }
        //handleScanResultsAvailable();
        mWifiManager.disconnect();
        EnableWifi wifiEnabler = new EnableWifi(getApplicationContext(), mNetworkSsidToConnect.trim(), passkey.trim(), wifiConnect.getWifiScanResutSecurity(mNetworkSsidToConnect.trim()), true);
        EnableWifi.setMAXRETRY(3);
        wifiEnabler.setWifiConnectionListener(new EnableWifi.OnWiFiConnectionListener() {

            public void onConnecting(String msg) {
                //We Didnt Do Anything Because we are already showing a Loader stating Connecting to <SSID Name>
                Logc.d("===onConnecting "+msg +" end");
            }

            public void onWiFiConnected(boolean is_connected_to_correct_ssid,
                                        String failure_reason_if_any) {
                if (bSACDEVICE) {
                    isLibreConnected = false;
                    if (is_connected_to_correct_ssid) {
                        tips("Status : Connected to " + mNetworkSsidToConnect + "Reason : " + failure_reason_if_any);
                    } else {
                        tips("Failure : Connection to " + mNetworkSsidToConnect + "Reason : " + failure_reason_if_any);
                    }
                    Logc.d("===onWiFiConnected" + is_connected_to_correct_ssid);
                    handleScanResultsAvailable();
                }
                closeLoader();
            }
        });
        wifiEnabler.execute();
    }

    private void init(){
        KeepAliveManager.getInstnce().resgisterDeviceOnlineListener(deviceOnlineListener);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
        handleScanResultsAvailable();
    }

    public void onScan(View view){
        apListView.setAdapter(adpter);
        isDiscoverDevice = false;
        apListView.setVisibility(View.VISIBLE);
        text.setVisibility(View.GONE);
        isLibreConnected = true;
        handleScanResultsAvailable();
    }

    public void onScanDevice(View view){

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                sendPostDataToInternet("2001","1","uuxia",true);
//            }
//        }).start();
        BasicModel model = new BasicModel();
        model.setCmd(0x0001);
        model.setMsg("This is scanning data..");
        String json = GsonUtils.pack(model);
        Toast.makeText(this,json,Toast.LENGTH_LONG).show();
        App.getInstance().scan(json);
    }


    public void handleScanResultsAvailable() {
        wifiConnect.clearWifiScanResult();
        wifiConnect.clearSacDevices();

        mWifiManager.startScan();
        List<ScanResult> list = mWifiManager.getScanResults();

        if (list != null) {
            for (int i = 0; i < list.size(); i++) {

                final ScanResult scanResult = list.get(i);
                if (scanResult == null) {
                    continue;
                }

                if (TextUtils.isEmpty(scanResult.SSID)) {
                    continue;
                }
                wifiConnect.putWifiScanResultSecurity(scanResult.SSID, scanResult.capabilities);
                if (scanResult.SSID.contains("LSConfigure_") || !isLibreConnected) {
                    wifiConnect.addSacDevices(scanResult.SSID);
                }
            }
        }

        if (wifiConnect.mSACDevicesList != null && wifiConnect.mSACDevicesList.size() == 0) {
            wifiConnect.addSacDevices(getResources().getString(R.string.title_no_sac_device));
        }
        if (adpter != null)
            adpter.notifyDataSetChanged();
    }

    private Handler wifihandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                //http post response
                case HTTP_POST_RESPONSE:
                    apListView.setVisibility(View.GONE);
                    text.setVisibility(View.VISIBLE);
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            connecToSSId(wifiConnect.getMainSSID(), wifiConnect.getMainSSIDPwd(), false);
                        }
                    },1000);
                    udpServer();
                    if (msg.arg1 == 200) {
                        text.setText("绑定成功...HTTP:" + msg.arg1);
                    }else if (msg.arg1 == -200){
                        Object obj = msg.obj;
                        text.setText("DoPost:"+obj);
                    }else{
                        Object obj = msg.obj;
                        text.setText("绑定失败...HTTP:" + msg.arg1+"\r\n"+obj);
                    }

                    break;
                case CONNECT_ORIGINAL_SSID:
//                    connecToSSId(wifiConnect.getMainSSID(), wifiConnect.getMainSSIDPwd(), false);
                    break;
                default:
                    break;
            }
        }
    };


    private Handler udpHander = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            PacketBuffer packet = (PacketBuffer) msg.obj;
            byte[] data = packet.getData();
            BasicModel basicModel = null;
            String ison = null;
            try {
                String json = new String(data, "GBK");
                basicModel = (BasicModel) GsonUtils.parse(json, BasicModel.class);
                LinkedTreeMap map = (LinkedTreeMap) basicModel.getData();
                ison = GsonUtils.pack(map);
                System.out.println(ison);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (basicModel == null)
                return;
            if (ison == null)
                return;
            switch (basicModel.getCmd()){
                //发现设备
                case CMD.HET_SCAN_REPIY:
                    DeviceModel deviceModel = (DeviceModel) GsonUtils.parse(ison, DeviceModel.class);
                    if (deviceModel != null){
                        if (!discoverDeviceSet.containsKey(deviceModel.getDeviceMac().toLowerCase())){
                            discoverDeviceSet.put(deviceModel.getDeviceMac().toLowerCase(), deviceModel);
                            System.out.println(deviceModel.toString());
                            deviceModel.setDeviceIp(packet.getIp());
                            mData.add(deviceModel);
                            apListView.setAdapter(deviceAdpter);
                            deviceAdpter.notifyDataSetChanged();
                            isDiscoverDevice = true;

                            apListView.setVisibility(View.VISIBLE);
                            text.setVisibility(View.GONE);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void udpServer(){
        App.getInstance().createUdpServer(new IRecevie() {
            @Override
            public void onRecevie(PacketBuffer packet) {
                Message msg = Message.obtain();
                msg.obj = packet;
                udpHander.sendMessage(msg);

               /* byte[] data = packet.getData();
                try {
                    String json = new String(data, "GBK");
                    BasicModel basicModel = (BasicModel) GsonUtils.parse(json, BasicModel.class);
                    LinkedTreeMap map = (LinkedTreeMap) basicModel.getData();
                    String ison = GsonUtils.pack(map);
                    Message msg = Message.obtain();
                    msg.what = basicModel.getCmd();
                    msg.obj = ison;
                    udpHander.sendMessage(msg);
                    System.out.println(ison);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }*/
            }
        });
    }


    class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isLibreConnected){
                handleScanResultsAvailable();
            }
//            List<ScanResult> list = mWifiManager.getScanResults();
//            for (ScanResult sr : list){
//                Logc.d("WifiReceiver="+sr.SSID +"  "+sr.level);
//            }
        }
    }
}
