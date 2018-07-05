package com.example.sunday.noicemonitor.decibel;

import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.IOException;


public class DecibelClient {

    private MediaRecorder mMediaRecorder;
    private static final int MAX_LENGTH = 1000 * 60 * 10;// 最大录音时长1000*60*10;
    private String filePath;

   private Handler handler;

   private int timeSlot;

    private long startTime;
    private long endTime;

    public DecibelClient(Handler handler,int timeSlot){
        this.handler=handler;
        this.filePath="/dev/null";
        this.timeSlot=timeSlot;
    }


    public void startRecord(){
        if(mMediaRecorder==null){
            mMediaRecorder=new MediaRecorder();
        }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setOutputFile(filePath);
        mMediaRecorder.setMaxDuration(MAX_LENGTH);
        try {
            Log.d("imTrying","trying");
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            startTime=System.currentTimeMillis();
            updateMicStatus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long stopRecord(){
        if(mMediaRecorder==null){
            return 0L;
        }
        endTime=System.currentTimeMillis();
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder=null;
        return endTime-startTime;
    }

    private Runnable mUpdateMicStatusTimer=new Runnable() {
        @Override
        public void run() {
          updateMicStatus();
        }
    };

    /*
    * 更新话筒状态*/

    private int BASE=1;

    private void updateMicStatus(){
        if(mMediaRecorder!=null){
            double ratio=(double)mMediaRecorder.getMaxAmplitude()/BASE;
            Log.d("update",ratio+"");
            double db;
            handler.postDelayed(mUpdateMicStatusTimer,timeSlot);
            if(ratio>1){
                db=(20*Math.log(ratio))*0.7;
                Message message=Message.obtain(handler);
                message.what=0x00;
                message.obj=db;
                handler.sendMessage(message);
                handler.postDelayed(mUpdateMicStatusTimer,timeSlot);
                Log.d("decible",db+"");
            }
        }
    }
}
