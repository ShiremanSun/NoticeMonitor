package com.example.sunday.noicemonitor.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

public class PhoneUtil {
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getPhoneIMEI(Context context) {
        String IMEI="";
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
       if(manager!=null){
           IMEI=manager.getDeviceId();
       }
       return IMEI;
   }
   public static String getPhoneName(){
       return Build.BRAND;
   }
}
