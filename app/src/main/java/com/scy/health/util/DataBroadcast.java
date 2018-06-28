package com.scy.health.util;

import android.content.Context;
import android.util.Log;

import com.scy.health.Interface.BluetoothInterface;
import com.scy.health.Interface.DataBroadcastInterface;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;

public class DataBroadcast implements BluetoothInterface{
    private Context context;
    private BlueTooth blueTooth;
    private static final String TAG = "DataBroadcast";
    private DataBroadcastInterface dataBroadcastInterface;
    private Timer timer;
    public DataBroadcast(Context context,DataBroadcastInterface dataBroadcastInterface){
        this.context = context;
        this.dataBroadcastInterface = dataBroadcastInterface;

        blueTooth = new BlueTooth(context);
        blueTooth.start(this);

//        //测试用
//        timer = new Timer();
//        timer.schedule(new TimerTask(){
//            public void run(){
//                //正常眼动：0.623567 0.599057 0 0 0.00358101  0.00353361
//                //疲劳眼动：0.830315 0.800068 0 0.145872 0.00421145 0.00368119
//                double[] eye = {0.623567,0.599057,0,0,0.00358101,0.00353361};
//                double[] eye2 = {0.830315,0.800068,0,0.145872,0.00421145,0.00368119};
//
//                int[] bp = new int[2];
////                bp[0] = new Random().nextInt(80)+80
////                bp[1] = new Random().nextInt(60)+50;
//                bp[0] = new Random().nextInt(30)+90;
//                bp[1] = new Random().nextInt(25)+60;
//
//                int temperature = new Random().nextInt(1)+36;
//                int heartbeat = new Random().nextInt(35)+60;
//                update(temperature,heartbeat,bp,eye2);
//            }
//        }, 0,1*1000);
    }

    private void update(float temperature,int heartbeat,int[] bp,double[] eye){
        dataBroadcastInterface.onaTemperatureChanged(temperature);
        dataBroadcastInterface.onHeartbeatChanged(heartbeat);
        dataBroadcastInterface.onBpChanged(bp);
        dataBroadcastInterface.onEyeChanged(eye);
        dataBroadcastInterface.onChanged(temperature,heartbeat,bp,eye);
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
        dataBroadcastInterface.onSuccess();
    }

    @Override
    public void onError(String errorData) {
        dataBroadcastInterface.onOverTime(errorData);
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
        double[] eye = {0.623567,0.599057,0,0,0.00358101,0.00353361};
        double[] eye2 = {0.830315,0.800068,0,0.145872,0.00421145,0.00368119};

        float temperature;
        int heartbeat;
        int[] bp = new int[2];
        bp[0] = new Random().nextInt(30)+90;
        bp[1] = new Random().nextInt(25)+60;
        String[] values = data.split(" ");
        temperature = Float.valueOf(values[0]);
        heartbeat = Integer.valueOf(values[1]);
        update(temperature,heartbeat,bp,eye2);
    }
}
