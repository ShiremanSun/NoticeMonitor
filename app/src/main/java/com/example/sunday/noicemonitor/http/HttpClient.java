package com.example.sunday.noicemonitor.http;

import android.content.Context;

import com.example.sunday.noicemonitor.decibel.DecibelClient;
import com.example.sunday.noicemonitor.location.LocateClient;

public class HttpClient {

    private final static int CONNECT_TIMEOUT = 60;
    private final static int READ_TIMEOUT = 100;
    private final static int WRITE_TIMEOUT = 60;
    private final static String url="www.baidu.com";
    private String IMEI;
    private DecibelClient decibelClient;
    private LocateClient locateClient;
    private Context context;
    public HttpClient(DecibelClient decibelClient,LocateClient locateClient,Context context){
        this.decibelClient=decibelClient;
        this.locateClient=locateClient;
        this.context=context;
    }
   public void start(){
        decibelClient.startRecord();
        locateClient.satrtLocate();
   }

   public void stop(){
        decibelClient.stopRecord();
        locateClient.stopLocate();
   }
}
