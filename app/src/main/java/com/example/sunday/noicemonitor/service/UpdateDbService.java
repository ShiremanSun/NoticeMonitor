package com.example.sunday.noicemonitor.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.example.sunday.noicemonitor.decibel.DecibelClient;
import com.example.sunday.noicemonitor.http.HttpClient;
import com.example.sunday.noicemonitor.location.LocateClient;

import java.lang.ref.WeakReference;

public class UpdateDbService extends Service {

    private final Binder mBinder=new MyBinder();

    private DecibelClient decibelClient;
    private HttpClient httpClient;

    public LocateClient getLocateClient() {
        return locateClient;
    }

    private LocateClient locateClient;

    private int timeSlot=1000;
    public UpdateDbService() {
    }
    @Override
    public void onCreate(){
        decibelClient =new DecibelClient(timeSlot);
        locateClient=new LocateClient(timeSlot,getApplicationContext());
        httpClient=new HttpClient(decibelClient,locateClient,getApplicationContext());
        httpClient.start();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        httpClient.stop();
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public class MyBinder extends Binder{
       public UpdateDbService getService(){
           return UpdateDbService.this;
       }
    }

    public DecibelClient getDecibelClient() {
        return decibelClient;
    }
}
