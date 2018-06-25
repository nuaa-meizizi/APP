package com.scy.health.util;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.scy.health.Interface.BluetoothInterface;
import com.scy.health.Interface.DataBroadcastInterface;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class DataBroadcast implements BluetoothInterface{
    private Context context;
    private BlueTooth blueTooth;
    private static final String TAG = "DataBroadcast";
    private DataBroadcastInterface dataBroadcastInterface;
    private Timer timer;
    public DataBroadcast(Context context,DataBroadcastInterface dataBroadcastInterface){
        this.context = context;
        this.dataBroadcastInterface = dataBroadcastInterface;

//        blueTooth = new BlueTooth(context);
//        blueTooth.start(this);

        //测试用
        timer = new Timer();
        timer.schedule(new TimerTask(){
            public void run(){
                int[] bp = new int[2];
                bp[0] = new Random().nextInt(80)+80;
                bp[1] = new Random().nextInt(60)+50;
                update(new Random().nextInt(2)+38,new Random().nextInt(3)+78,bp);
            }
        }, 0,1*1000);
    }

    private void update(float temperature,int heartbeat,int[] bp){
        dataBroadcastInterface.onaTemperatureChanged(temperature);
        dataBroadcastInterface.onHeartbeatChanged(heartbeat);
        dataBroadcastInterface.onBpChanged(bp);
        dataBroadcastInterface.onChanged(temperature,heartbeat,bp);
    }

    public void destroy(){
        if (blueTooth != null) {
            try {
                blueTooth.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        timer.cancel();
    }

    @Override
    public void onSuccess() {
        Toast.makeText(context,"蓝牙连接成功",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String errorData) {
        Toast.makeText(context,"蓝牙连接错误："+errorData,Toast.LENGTH_SHORT).show();
        try {
            blueTooth.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "onError: "+errorData);
    }

    @Override
    public void onReceive(String data) {
        //可以在这里访问网络请求虚拟参数
        float temperature;
        int heartbeat;
        int[] bp = new int[2];
        bp[0] = new Random().nextInt(80)+80;
        bp[1] = new Random().nextInt(60)+50;        String[] values = data.split(" ");
        temperature = Float.valueOf(values[0]);
        heartbeat = Integer.valueOf(values[1]);
        update(temperature,heartbeat,bp);
    }
}
