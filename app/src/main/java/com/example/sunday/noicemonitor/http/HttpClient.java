package com.example.sunday.noicemonitor.http;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import com.example.sunday.noicemonitor.Database.MySQLiteOpenHelper;
import com.example.sunday.noicemonitor.decibel.DecibelClient;
import com.example.sunday.noicemonitor.location.LocateClient;
import com.example.sunday.noicemonitor.model.LocationBean;
import com.example.sunday.noicemonitor.util.PhoneUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpClient implements DecibelClient.CallBack {

   private String format="yyyyMMddHHmmss";
    private final static String url="http://47.93.225.63:8080/noise_monitor/reciveNoise/recive";
    private final static String REGISTER= "http://47.93.225.63:8080/noise_monitor/registerMobile/register_mobile";
    private boolean isRunning;
    private static final int STORAGE_TIME=1000*60*30;
    private String IMEI;
    private OkHttpClient okHttpClient=new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10,TimeUnit.SECONDS)
            .readTimeout(10,TimeUnit.SECONDS)
            .build();
    private DecibelClient decibelClient;
    private SQLiteDatabase mDb;
    private LocateClient locateClient;
    private LocationBean locationBean;
    private double dbl;
    private final Object mLock;
    private Thread mThread;
    private SharedPreferences preferences;
    private static final String RESISTERINFO="registerinfo";
    private static final String IMEIINFO="IMEI";


    private Context context;
    public HttpClient(DecibelClient decibelClient,LocateClient locateClient,Context context){
        this.decibelClient=decibelClient;
        this.locateClient=locateClient;
        this.context=context;
        IMEI= PhoneUtil.getPhoneIMEI(context);
        decibelClient.registerCallBack(this);
        mLock=new Object();
        mThread=new MyThread();
        preferences=context.getSharedPreferences(RESISTERINFO,Context.MODE_PRIVATE);
        mDb= MySQLiteOpenHelper.getInstance(context);
    }
   public void start(){
        isRunning=true;
        register();
        decibelClient.getNoiseLevel();
        locateClient.startLocate();
        mThread.start();
   }

   public void stop(){
        isRunning=false;
        decibelClient.stop();
        locateClient.stopLocate();
   }
  private void sentForm(double db, LocationBean locationBean) throws Exception{
      FormBody formBody=new FormBody.Builder()
              .add("IMEI",IMEI)
              .add("longitude", String.valueOf(locationBean.getLongitude()))
              .add("latitude",String.valueOf(locationBean.getLatitude()))
              .add("decibels",String.format(Locale.CHINA,"%.0f",db))
              .add("time",getTime(System.currentTimeMillis()))
              .build();
      Log.d("longitude",locationBean.getLatitude()+"");
      Log.d("longitude",locationBean.getLongitude()+"");
      Request request=new Request.Builder()
              .url(url)
              .post(formBody)
              .build();

      Call call= okHttpClient.newCall(request);
      call.enqueue(new Callback() {
         @Override
         public void onFailure(Call call, IOException e) {
             Log.d("call",e.getMessage());
         }

         @Override
         public void onResponse(Call call, Response response) throws IOException {
             Log.d("call",response.code()+"");
             Log.d("call",response.body().string()+"");
         }
     });

  }
  private class MyThread extends Thread{
      @Override
      public void run() {
          while (isRunning){

              synchronized (mLock){
                  try {
                      mLock.wait(1000*30);
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
              }
              try {
                  sentForm(dbl,locationBean);
              } catch (Exception e) {
                  e.printStackTrace();
              }

          }
      }
  }
  //注册手机信息
  private void register(){
        String IMEI=preferences.getString(IMEIINFO,"empty");
        if(IMEI.equals("empty")) {
            OkHttpClient okHttpClient1 = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(IMEIINFO, PhoneUtil.getPhoneIMEI(context));
            editor.apply();
            FormBody formBody = new FormBody.Builder()
                    .add("IMEI", PhoneUtil.getPhoneIMEI(context))
                    .add("mobile_name", PhoneUtil.getPhoneName())
                    .add("years", getTime(Build.TIME))
                    .add("time", getTime(System.currentTimeMillis()))
                    .build();
            final Request request = new Request.Builder()
                    .url(REGISTER)
                    .post(formBody)
                    .build();
            Call call = okHttpClient1.newCall(request);
            Log.d("callback", "callback");
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("failure", e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d("failure", response.code() + "");
                    Log.d("failure", response.body().string());
                }
            });
        }

  }
  private String getTime(long time){
      SimpleDateFormat sdf=new SimpleDateFormat(format,Locale.CHINA);
      return sdf.format(new Date(time));
  }
    @Override
    public void callBack(double db) {
        locationBean=locateClient.getLocation();
        dbl=db;

    }

    @Override
    public void updateChart(double db) {

    }
}
