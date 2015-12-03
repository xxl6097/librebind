package com.het.librebind;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.het.librebind.model.DeviceModel;

import java.util.List;

/**
 * Created by Android Studio.
 * Author: uuxia
 * Date: 2015-12-01 20:03
 * Description:
 */
/*
 * -----------------------------------------------------------------
 * Copyright ?2014 clife - �Ͷ�̩�Ҿ���������Ƽ����޹�˾
 * Shenzhen H&T Intelligent Control Co.,Ltd.
 * -----------------------------------------------------------------
 *
 * File: Adpter.java
 * Create: 2015/12/1 20:03
 */
public class Adpter extends BaseAdapter {

    private Context c;
    private List<DeviceModel> mList;
    private LayoutInflater mInflater;

    public Adpter(Context c, List<DeviceModel> mList) {
        this.c = c;
        this.mList = mList;
        this.mInflater = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        DeviceModel dm = mList.get(position);
        //�۲�convertView��ListView�������
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.device_item,null);
            holder = new Holder();
            /**�õ������ؼ��Ķ���*/
            holder.ip = (TextView) convertView.findViewById(R.id.ip);
            holder.deviceType = (TextView) convertView.findViewById(R.id.devicesubtype);
            holder.deviceSubType = (TextView) convertView.findViewById(R.id.devicesubtype);
            holder.brandId = (TextView) convertView.findViewById(R.id.brandid);
            convertView.setTag(holder);//��ViewHolder����
        }
        else{
            holder = (Holder)convertView.getTag();//ȡ��ViewHolder����
        }
        /**����TextView��ʾ�����ݣ������Ǵ���ڶ�̬�����е�����*/
        if (dm != null) {
            holder.ip.setText(dm.getDeviceIp());
            holder.deviceType.setText(dm.getDeviceType());
            holder.deviceSubType.setText(dm.getDeviceSubType());
            holder.deviceType.setText(dm.getBrandId());
        }


        return convertView;
    }

    static class Holder{
        TextView ip,deviceType,deviceSubType,brandId;
    }
}
