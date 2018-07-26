package com.scy.health.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.scy.health.Interface.BluetoothInterface;
import com.scy.health.Interface.DataBroadcastInterface;
import com.scy.health.SimulationService;

import java.io.IOException;

import static android.content.Context.BIND_AUTO_CREATE;

public class DataBroadcast implements BluetoothInterface{
    private Context context;
    private BlueTooth blueTooth;
    private static final String TAG = "DataBroadcast";
    private DataBroadcastInterface dataBroadcastInterface;
    private SimulationService simulationService;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            simulationService = ((SimulationService.MyBinder)binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public DataBroadcast(Context context,DataBroadcastInterface dataBroadcastInterface){
        this.context = context;
        this.dataBroadcastInterface = dataBroadcastInterface;

        blueTooth = new BlueTooth(context);
        blueTooth.start(this);

        //绑定模拟数据service
        Intent intent = new Intent(context, SimulationService.class);
        context.bindService(intent, conn, BIND_AUTO_CREATE);
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
        if (conn != null) {
            Log.i(TAG, "destroy: simulationService应该被解绑了");
            context.unbindService(conn);
            simulationService = null;
            conn = null;
        }
    }

    @Override
    public void onSuccess() {
        dataBroadcastInterface.onSuccess();
    }

    @Override
    public void onError(String errorData) {
//        dataBroadcastInterface.onOverTime(errorData);
        try {
            blueTooth.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "onError: "+errorData);
        Toast.makeText(context,"蓝牙打开失败，将全部采用模拟数据",Toast.LENGTH_SHORT).show();
        dataBroadcastInterface.onSuccess();
        getDataFromService();
    }

    private void getDataFromService(){
        if (simulationService != null){
            simulationService.setDataBroadcastInterface(new DataBroadcastInterface() {
                @Override
                public void onaTemperatureChanged(float temperature) {

                }

                @Override
                public void onHeartbeatChanged(int heartbeat) {

                }

                @Override
                public void onBpChanged(int[] bp) {

                }

                @Override
                public void onChanged(float temperature, int heartbeat, int[] bp, double[] eye) {
                    update(temperature,heartbeat,bp,eye);           //回调数据
                }

                @Override
                public void onEyeChanged(double[] eye) {

                }

                @Override
                public void onSuccess() {

                }

                @Override
                public void onOverTime(String error) {

                }
            });
        }
    }

    @Override
    public void onReceive(String data) {
        double[] eye = new double[6];
        float temperature;
        int heartbeat;
        int[] bp = new int[2];

        if (simulationService!=null) {
            eye = simulationService.getEye();
            bp = simulationService.getBp();
        }

        String[] values = data.split(" ");
        temperature = Float.valueOf(values[0]);
        heartbeat = Integer.valueOf(values[1]);
        update(temperature,heartbeat,bp,eye);
    }
}
