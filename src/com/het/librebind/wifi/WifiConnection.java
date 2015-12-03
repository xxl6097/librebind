package com.het.librebind.wifi;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by karunakaran on 7/23/2015.
 */
public class WifiConnection {

    private static WifiConnection instance = new WifiConnection();
    protected WifiConnection() {
        // Exists only to defeat instantiation.
    }
    public static WifiConnection getInstance() {
        if(instance == null) {
            instance = new WifiConnection();
        }
        return instance;
    }

    public boolean ismSendWifiData() {
        return mSendWifiData;
    }

    public void setmSendWifiData(boolean mSendWifiData) {
        this.mSendWifiData = mSendWifiData;
    }

    public String getMainSSID() {
        return mainSSID;
    }

    public boolean mSACDevicePostDone = false;

    public void setmSACDevicePostDone(boolean mStatus){
        this.mSACDevicePostDone = mStatus;
    }

    public boolean getmSACDevicePostDone(){
        return mSACDevicePostDone;
    }

    public void setMainSSID(String mainSSID) {
        this.mainSSID = mainSSID;
    }

    public String getMainSSIDPwd() {
        return mainSSIDPwd;
    }

    public void setMainSSIDPwd(String mainSSIDPwd) {
        this.mainSSIDPwd = mainSSIDPwd;
    }

    public String getMainSSIDSec() {
        return mainSSIDSec;
    }

    public void setMainSSIDSec(String mainSSIDSec) {
        this.mainSSIDSec = mainSSIDSec;
    }

    public void setMainSSIDDetails(String ssid,String password,String security){
        this.mainSSID=ssid;
        this.mainSSIDPwd=password;
        this.mainSSIDSec=security;
    }

    public HashMap<String,String> ssidDeviceNameSAC= new HashMap<String,String>();

    public void putssidDeviceNameSAC(String key,String value){
        ssidDeviceNameSAC.put(key,value);
    }

    public void clearssidDeviceNameSAC(){
        ssidDeviceNameSAC.clear();
    }

    public String getssidDeviceNameSAC(String key){
        return ssidDeviceNameSAC.get(key);
    }

    public String mainSSID = "";

    public String mainSSIDPwd="";

    public String mainSSIDSec="";

    public String deviceName="HeT";

    public boolean mSendWifiData = false;
    public String TAG="WifiConnection";
    public static Context ctx;



    public List<String> mSACDevicesList = new ArrayList<String>() ;

    public HashMap<String,String> WifiScanSSIDSecurityMap = new HashMap<String,String>();

    public String getWifiScanResutSecurity(String key){
            return WifiScanSSIDSecurityMap.get(key);
    }
    public String getSSIDForSending(String cap){
        if(cap!=null) {
            if (cap.contains("WEP")) {
                return "WEP";
            } else if (cap.contains("WPA")) {
                //Security.add("WPA-PSK");
                return "WPA-PSK";
            } else {
                return "NONE";

            }
        }return "";
    }
    public void putWifiScanResultSecurity(String key,String value){
        WifiScanSSIDSecurityMap.put(key, value);
    }

    public void clearWifiScanResult(){
        WifiScanSSIDSecurityMap.clear();
    }
    public String[] getAllSSIDs(){
        String tArray[] = WifiScanSSIDSecurityMap.keySet().toArray(new String[0]);
        return tArray;
    }
    public String[] getAllSSIDList(){
        return WifiScanSSIDSecurityMap.keySet().toArray(new String[0]);

    }

    public ArrayList<String> getFilteredSSIDsArrayList(){
        String tArray[] = WifiScanSSIDSecurityMap.keySet().toArray(new String[0]);
        ArrayList<String> mylist = new ArrayList<String>();

        for(int i=0,j=0;i<tArray.length;i++){
            if(! tArray[i].contains("LSConfigure_"))
                mylist.add(tArray[i]);
        }
        return mylist;
    }

    public String[] getFilteredSSIDs(){
        String tArray[] = WifiScanSSIDSecurityMap.keySet().toArray(new String[0]);
        ArrayList<String> mylist = new ArrayList<String>();

        for(int i=0,j=0;i<tArray.length;i++){
            if(! tArray[i].contains("LSConfigure_"))
                mylist.add(tArray[i]);
        }
        return mylist.toArray(new String[0]);
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public List<String> getFilteredSSIDsList(){
        String tArray[] = WifiScanSSIDSecurityMap.keySet().toArray(new String[0]);
        ArrayList<String> mylist = new ArrayList<String>();

        for(int i=0,j=0;i<tArray.length;i++){
          ///  if(! tArray[i].contains("LSConfigure_"))
                mylist.add(tArray[i]);
        }
        return mylist;
    }

    public WifiConnection(Context ctx){
        ctx=ctx;
    }

    public int getNumbSacDevicesList(){
        return mSACDevicesList.size();
    }

    public int addSacDevices(String mSacDevice){
        if(!mSACDevicesList.contains(mSacDevice))
            mSACDevicesList.add(mSacDevice);
        return mSACDevicesList.size();
    }

    public String[] getAllSacDevice(){


        String tArray[] = mSACDevicesList.toArray(new String[0]);
        return tArray;
    }

    public List<String> getAllSacDeviceList(){
        return mSACDevicesList;
    }
    public String getSacDevice(int mPosition){
        return mSACDevicesList.get(mPosition);
    }

    public int clearSacDevices(){
        mSACDevicesList.clear();
        return mSACDevicesList.size();
    }


}
