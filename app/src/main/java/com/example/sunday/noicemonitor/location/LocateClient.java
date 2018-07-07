package com.example.sunday.noicemonitor.location;

import android.content.Context;
import android.util.Log;


import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.sunday.noicemonitor.model.LocationBean;

public class LocateClient {
    private LocationClient mLocationClient;
    private  double latitude;
    private  double longitude;

    private  MyLocationListener myLocationListener=new MyLocationListener();

    public LocateClient(int timeSlot,Context context){
        mLocationClient=new LocationClient(context);
        mLocationClient.registerLocationListener(myLocationListener);
        LocationClientOption option=new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd0911");
        //设置扫描时间间隔
        option.setScanSpan(timeSlot*10);
        option.setOpenGps(true);
        //服务停止的时候杀死进程
        option.setIgnoreKillProcess(false);
        //可选，设置是否收集Crash信息，默认收集，即参数为false
        option.SetIgnoreCacheException(false);
        mLocationClient.setLocOption(option);
    }

    public  LocationBean getLocation(){

        LocationBean locationBean=new LocationBean();
        locationBean.setLatitude(latitude);
        locationBean.setLongitude(longitude);

        return locationBean;

    }
    public  class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

            latitude = location.getLatitude();    //获取纬度信息
            longitude = location.getLongitude();    //获取经度信息

            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明

            Log.d("latitude",latitude+"");
        }
    }

    public void startLocate(){

        Log.d("locationStart","locationStart");
        mLocationClient.restart();
    }

    public void stopLocate(){
        mLocationClient.stop();
    }
}
