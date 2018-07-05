package com.example.sunday.noicemonitor.service;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.example.sunday.noicemonitor.MainActivity;
import com.example.sunday.noicemonitor.decibel.DecibelClient;
import com.example.sunday.noicemonitor.http.HttpClient;
import com.example.sunday.noicemonitor.location.LocateClient;
import com.example.sunday.noicemonitor.util.IMEIUtil;

import java.lang.ref.WeakReference;

public class UpdateDbService extends Service {

    private final Binder mBinder=new MyBinder();

    private DecibelClient decibelClient;
    private HttpClient httpClient;
    private LocateClient locateClient;


    private Handler mHandler;


    private Callback callback;

    private int timeSlot=1000*30;
    public UpdateDbService() {

    }

    @Override
    public void onCreate(){
        mHandler=new MyHandler(this);
        decibelClient=new DecibelClient(mHandler,timeSlot);
        locateClient=new LocateClient(timeSlot);
        httpClient=new HttpClient(decibelClient,locateClient,this);
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

    private static class MyHandler extends Handler{
        private WeakReference<Service> weakReference;
        MyHandler(Service service){
            weakReference=new WeakReference<>(service);
        }
         double db;
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==0){
                db= (double) msg.obj;
                UpdateDbService updateDbService= (UpdateDbService) weakReference.get();
                if(updateDbService!=null){
                    if(updateDbService.callback!=null){
                        updateDbService.callback.updateDb(db);
                    }
                }



            }
        }
    }


    public void registerCallback(Callback callback){
        this.callback=callback;
    }

    public void unregisterCallback(){
        this.callback=null;
    }

    public interface Callback{
        void updateDb(double db);
    }


}
