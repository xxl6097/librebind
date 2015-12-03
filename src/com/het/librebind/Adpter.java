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
 * Copyright ?2014 clife - 和而泰家居在线网络科技有限公司
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
        //观察convertView随ListView滚动情况
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.device_item,null);
            holder = new Holder();
            /**得到各个控件的对象*/
            holder.ip = (TextView) convertView.findViewById(R.id.ip);
            holder.deviceType = (TextView) convertView.findViewById(R.id.devicesubtype);
            holder.deviceSubType = (TextView) convertView.findViewById(R.id.devicesubtype);
            holder.brandId = (TextView) convertView.findViewById(R.id.brandid);
            convertView.setTag(holder);//绑定ViewHolder对象
        }
        else{
            holder = (Holder)convertView.getTag();//取出ViewHolder对象
        }
        /**设置TextView显示的内容，即我们存放在动态数组中的数据*/
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
