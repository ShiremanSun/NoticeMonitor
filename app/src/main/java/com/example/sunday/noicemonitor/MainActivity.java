package com.example.sunday.noicemonitor;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Entity;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sunday.noicemonitor.decibel.DecibelClient;
import com.example.sunday.noicemonitor.location.LocateClient;
import com.example.sunday.noicemonitor.model.LocationBean;
import com.example.sunday.noicemonitor.service.UpdateDbService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements DecibelClient.CallBack{


    private DecibelClient decibelClient;
    private LocateClient locateClient;
    private List<String> mPermissions;


    private LocationBean locationBean;

    private UpdateDbService mService;
    private ImageView needleImage;
    private TextView dbText;

    private List<Entity> noiseData;


    private double degree=0;
    private Intent intent;
    private ServiceConnection connection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        needleImage=findViewById(R.id.needle);
        dbText=findViewById(R.id.textView);


        mPermissions=new ArrayList<>();



        connection=new MyServiceConnection();


        intent=new Intent(this, UpdateDbService.class);

        String[] permissions=new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_PHONE_STATE,Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE};

        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    mPermissions.add(permission);
                }
            }
            if(!mPermissions.isEmpty()){
                ActivityCompat.requestPermissions(this,mPermissions.toArray(new String[mPermissions.size()]),1);
            }else {
                startService(intent);
                bindService(intent,connection,Service.BIND_AUTO_CREATE);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode==1){
            for(int result:grantResults){
                if(result!=PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"权限未申请，无法使用",Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            startService(intent);
            bindService(intent,connection,Service.BIND_AUTO_CREATE);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void callBack(final double db) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
              dbText.setText(String.format(Locale.CHINA,"%.0fdb",db));
                double newDeg=db*2;
                RotateAnimation animation=new RotateAnimation((float)degree,(float)newDeg,
                        Animation.RELATIVE_TO_SELF,0.5f,
                        Animation.RELATIVE_TO_SELF,0.5f);
                animation.setFillAfter(true);
                animation.setDuration(1000);
                needleImage.startAnimation(animation);
                degree=newDeg;


            }
        });
    }

    private class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UpdateDbService.MyBinder binder= (UpdateDbService.MyBinder) service;
            mService=binder.getService();
            decibelClient=mService.getDecibelClient();
            locateClient=mService.getLocateClient();
            decibelClient.registerCallBack(MainActivity.this);

        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
           decibelClient.unregisterCallBack(MainActivity.this);
        }
    }

    @Override
    protected void onDestroy() {
        decibelClient.unregisterCallBack(MainActivity.this);
        stopService(intent);
        unbindService(connection);
        super.onDestroy();
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
