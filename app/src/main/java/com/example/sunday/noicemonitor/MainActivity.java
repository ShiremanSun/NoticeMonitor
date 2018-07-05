package com.example.sunday.noicemonitor;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sunday.noicemonitor.decibel.DecibelClient;
import com.example.sunday.noicemonitor.service.UpdateDbService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    DecibelClient decibelClient;

    private List<String> mPermissions;

    private UpdateDbService mService;

    private Intent intent;
    private ServiceConnection connection;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPermissions=new ArrayList<>();

        textView=findViewById(R.id.text);

        connection=new MyServiceConnection();


        intent=new Intent(this, UpdateDbService.class);


        String[] permissions=new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.READ_PHONE_STATE,Manifest.permission.RECORD_AUDIO,
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

    private class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UpdateDbService.MyBinder binder= (UpdateDbService.MyBinder) service;
            mService=binder.getService();
            mService.registerCallback(new UpdateDbService.Callback() {
                @Override
                public void updateDb(double db) {
                    textView.setText(String.format("%s", db));
                }
            });

        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
           mService.unregisterCallback();
        }
    }

    @Override
    protected void onDestroy() {
       mService.unregisterCallback();
        super.onDestroy();
    }
}
