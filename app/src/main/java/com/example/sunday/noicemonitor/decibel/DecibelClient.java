package com.example.sunday.noicemonitor.decibel;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DecibelClient {
    private List<CallBack> mCallBacks;
    private static final String TAG = "AudioRecord";
    static final int SAMPLE_RATE_IN_HZ = 8000;
    static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
    private AudioRecord mAudioRecord;
    private boolean isGetVoiceRun;
    private final Object mLock;

    private double db;
    private int timeSlot;


    public DecibelClient(int timeSlot) {
        mLock = new Object();
        this.timeSlot=timeSlot;
        mCallBacks=new ArrayList<>(2);
    }

    public double getDb(){
        return db;
    }
    public void registerCallBack(CallBack callBack){
       if(!mCallBacks.contains(callBack)){
           mCallBacks.add(callBack);
       }
    }

    public void unregisterCallBack(CallBack callBack){
        mCallBacks.remove(callBack);
    }
    private void removeCallbacks(){
        mCallBacks.clear();
    }

    public void stop() {
        removeCallbacks();
        this.isGetVoiceRun = false;
    }

    public void getNoiseLevel() {
        if (isGetVoiceRun) {
            return;
        }
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);

        isGetVoiceRun = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                mAudioRecord.startRecording();
                short[] buffer = new short[BUFFER_SIZE];
                while (isGetVoiceRun) {
                    int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
                    long v = 0;
                    for (short aBuffer : buffer) {
                        v += aBuffer * aBuffer;
                    }
                    double mean = v / (double) r;
                    final double volume = 10 * Math.log10(mean);
                    db=volume;

                    Log.d(TAG, "db value:" + volume);
                    // 根据需求的时间更新
                    synchronized (mLock) {
                        try {
                            mLock.wait(timeSlot);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    for(CallBack callBack:mCallBacks){
                        callBack.callBack(volume);
                    }
                }
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;

            }
        }).start();
    }
    public interface CallBack{
        void callBack(double db);
    }
}
