package com.example.sunday.noicemonitor;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sunday.noicemonitor.decibel.DecibelClient;
import com.example.sunday.noicemonitor.service.UpdateDbService;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

public class MainActivity extends AppCompatActivity implements DecibelClient.CallBack{


    private boolean isRunning;
    private List<Entry> pointValues;
    private DecibelClient decibelClient;
    private List<String> mPermissions;
    private int time=0;
    private Timer timer;

    private UpdateDbService mService;
    private ImageView needleImage;
    private TextView dbText;

    private final Object mLock=new Object();

    private LineChart dbChart;


    private double degree=0;
    private Intent intent;
    private ServiceConnection connection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        needleImage=findViewById(R.id.needle);
        dbText=findViewById(R.id.textView);
        dbChart=findViewById(R.id.dbchart);
        timer=new Timer();


        isRunning=true;
        initData();
        initChart();

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

    @Override
    public void updateChart(final double db) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(time<=60){

                    addEntry(db,time);
                    time+=10;
                }else {
                    addEntry(db,time);
                }
            }
        });

    }

    private class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UpdateDbService.MyBinder binder= (UpdateDbService.MyBinder) service;
            mService=binder.getService();
            decibelClient=mService.getDecibelClient();
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
        timer.cancel();
        isRunning=false;
        super.onDestroy();
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void initData(){
        pointValues=new ArrayList<>();


        LineDataSet lineDataSet = new LineDataSet(pointValues, "分贝时间线");
        //设置该线的颜色
        lineDataSet.setColor(Color.RED);
        //设置每个点的颜色
        lineDataSet.setCircleColor(Color.YELLOW);
        //设置该线的宽度
        lineDataSet.setLineWidth(1f);
        //设置每个坐标点的圆大小
        lineDataSet.setCircleSize(4f);
        //设置是否画圆
        lineDataSet.setDrawCircles(true);

        //设置字体颜色
        lineDataSet.setValueTextColor(Color.RED);
        //设置字体大小
        lineDataSet.setValueTextSize(12f);
        // 设置平滑曲线模式
        //  lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        //设置线一面部分是否填充颜色
        lineDataSet.setDrawFilled(true);
        //设置填充的颜色
        lineDataSet.setFillColor(Color.BLUE);
        //设置是否显示点的坐标值
        lineDataSet.setDrawValues(true);

        //线的集合（可单条或多条线）
        List<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);

        List<String> xLables=new ArrayList<>();
        xLables.add("0");
        xLables.add("10");
        xLables.add("20");
        xLables.add("30");
        xLables.add("40");
        xLables.add("50");
        xLables.add("60");

        //把要画的所有线(线的集合)添加到LineData里
        LineData lineData = new LineData(xLables,dataSets);
        //把最终的数据setData
        dbChart.setData(lineData);

    }
    private void initChart(){
        //加边框
        dbChart.setDrawBorders(true);
        //标题

        dbChart.setDescription("1小时分贝图");

        dbChart.setNoDataText("还没有数据");

        //设置是否可以触摸
        dbChart.setTouchEnabled(false);
        //设置是否可以拖拽
        dbChart.setDragEnabled(false);

        XAxis xAxis=dbChart.getXAxis();
        xAxis.setEnabled(true);
        //是否启用X轴
             xAxis.setEnabled(true);
        //        //是否绘制X轴线
           xAxis.setDrawAxisLine(true);
        //        //设置X轴上每个竖线是否显示
              xAxis.setDrawGridLines(true);
        //        //设置是否绘制X轴上的对应值(标签)
               xAxis.setDrawLabels(true);
        //        //设置X轴显示位置
               xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //        //设置竖线为虚线样式
              // xAxis.enableGridDashedLine(10f, 10f, 0f);
        //        //设置x轴标签数

        //        //图表第一个和最后一个label数据不超出左边和右边的Y轴
                xAxis.setAvoidFirstLastClipping(true);



        //=================设置左边Y轴===============
        YAxis axisLeft = dbChart.getAxisLeft();
        //是否启用左边Y轴
        axisLeft.setEnabled(true);
        //设置最小值（这里就按demo里固死的写）
        axisLeft.setAxisMinValue(0);
        //设置最大值（这里就按demo里固死的写了）
        axisLeft.setAxisMaxValue(120);
        //设置横向的线为虚线
        axisLeft.enableGridDashedLine(10f, 10f, 0f);
        //axisLeft.setDrawLimitLinesBehindData(true);

        //====================设置右边的Y轴===============
        YAxis axisRight = dbChart.getAxisRight();
        //是否启用右边Y轴
        axisRight.setEnabled(false);

        //设置限制线 40代表某个该轴某个值，也就是要画到该轴某个值上
        LimitLine limitLine = new LimitLine(40);
        //设置限制线的宽
        limitLine.setLineWidth(1f);
        //设置限制线的颜色
        limitLine.setLineColor(Color.RED);
        //设置基线的位置
        limitLine.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        limitLine.setLabel("安静线");
        limitLine.setTextSize(15);
        //设置限制线为虚线
        limitLine.enableDashedLine(10f, 10f, 0f);
        //左边Y轴添加限制线
        axisLeft.addLimitLine(limitLine);

    }

    private void addEntry(double db,int time){
          LineData lineData=dbChart.getLineData();
          if(lineData!=null){
              if(time<=60){
                  LineDataSet lineDataSet=lineData.getDataSetByIndex(0);
                  int numOfValues=lineDataSet.getEntryCount();
                  Log.d("update",numOfValues+"");
                  lineData.addEntry(new Entry((float) db,numOfValues),0);
                  dbChart.notifyDataSetChanged();
                  dbChart.invalidate();
              }else {
                  //移除第一个数据
                 LineDataSet lineDataSet=  lineData.getDataSetByIndex(0);
                 for(int i=1;i<lineDataSet.getEntryCount();i++){
                     lineDataSet.getEntryForXIndex(i-1).setVal(lineDataSet.getEntryForXIndex(i).getVal());
                 }
                  lineDataSet.getEntryForXIndex(6).setVal((float) db);
                  lineData.notifyDataChanged();
                  Log.d("update",lineDataSet.getEntryCount()+"");
                  dbChart.notifyDataSetChanged();
                  dbChart.invalidate();
              }

          }
    }





}
