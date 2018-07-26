package com.scy.health;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.scy.health.Interface.DataBroadcastInterface;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.Call;

import static com.scy.health.util.AppUtils.isAppRunning;

public class SimulationService extends Service implements Runnable{
    private Thread mThread;
    private static final String TAG = "SimulationService";

    public void setDataBroadcastInterface(DataBroadcastInterface dataBroadcastInterface) {
        this.dataBroadcastInterface = dataBroadcastInterface;
    }

    private DataBroadcastInterface dataBroadcastInterface;

    private Float temperature;
    private int heartbeat;
    private int[] bp = new int[2];
    private double[] eye = new double[6];
    private int weight;

    public Float getTemperature() {
        return temperature;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    public int[] getBp() {
        return bp;
    }

    public double[] getEye() {
        return eye;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public void run() {
        while (true)
        {
            SharedPreferences sharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE);
            String token = sharedPreferences.getString("token","anony");
            if (isAppRunning(this,"com.scy.health") ){
                OkHttpUtils
                        .get()
                        .url("http://app.logicjake.xyz:8080/health/fake/fakedata")
                        .addParams("token", token)
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                Log.e(TAG, "onError:获取模拟数据失败");
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                try {
                                    JSONObject res = new JSONObject(response);
                                    temperature = (float)res.getJSONObject("data").getDouble("temperature");
                                    heartbeat = res.getJSONObject("data").getInt("heartbeat");
                                    bp[0] = res.getJSONObject("data").getInt("bp0");
                                    bp[1] = res.getJSONObject("data").getInt("bp1");
                                    weight = res.getJSONObject("data").getInt("weight");
                                    JSONArray responseEye = res.getJSONObject("data").getJSONArray("eye");
                                    for (int i = 0; i < responseEye.length(); i++)
                                        eye[i] = responseEye.getDouble(i);

                                    if (dataBroadcastInterface!=null)
                                        dataBroadcastInterface.onChanged(temperature,heartbeat,bp,eye);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
            }
            try {
                Thread.sleep(1*1000);            //每隔1s发一次包
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public SimulationService() {
    }

    public class MyBinder extends Binder {
        public SimulationService getService(){
            return SimulationService.this;
        }
    }

    //通过binder实现调用者client与Service之间的通信
    private MyBinder binder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
//        if (isServiceRunning("com.scy.health.SimulationService")){
//            return null;
//        }
        Log.i(TAG, "onBind, Thread: " + Thread.currentThread().getName());
        mThread = new Thread(this);
        mThread.start();
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind, from:" + intent.getStringExtra("from"));
        mThread.interrupt();
        return false;
    }

    private boolean isServiceRunning(final String className) {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> info = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (info == null || info.size() == 0) return false;
        for (ActivityManager.RunningServiceInfo aInfo : info) {
            if (className.equals(aInfo.service.getClassName())) return true;
        }
        return false;
    }
}
