package com.example.sunday.noicemonitor.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

public class IMEIUtil {
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getPhoneIMEI(Context context) {
        String IMEI="";
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
       if(manager!=null){
           IMEI=manager.getDeviceId();
       }

       return IMEI;
   }
}
