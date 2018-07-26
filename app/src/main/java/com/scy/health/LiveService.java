package com.scy.health;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.List;

import okhttp3.Call;

import static com.scy.health.util.AppUtils.isAppRunning;

public class LiveService extends Service implements Runnable{
    private Thread mThread;
    private static final String TAG = "LiveService";

    @Override
    public void run() {
        while (true)
        {
            SharedPreferences sharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE);
            String IMEI = sharedPreferences.getString("imei",null);
            if (isAppRunning(this,"com.scy.health") && IMEI != null && IMEI.length() !=0 ){
                OkHttpUtils
                        .get()
                        .url("http://app.logicjake.xyz:8080/health/console/iamlive")
                        .addParams("imei", IMEI)
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                Log.e(TAG, "onError: 心跳包发送失败");
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                Log.i(TAG, "onResponse：心跳包发送成功");
                            }
                        });
            }
            try {
                Thread.sleep(5*60*1000);            //每隔5min发一次包
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public class MyBinder extends Binder {
        public LiveService getService(){
            return LiveService.this;
        }
    }
    //通过binder实现调用者client与Service之间的通信
    private MyBinder binder = new MyBinder();

    public LiveService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (isServiceRunning("com.scy.health.LiveService")){
            return null;
        }
        Log.i(TAG, "onBind, Thread: " + Thread.currentThread().getName());
        mThread = new Thread(this);
        mThread.start();
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind, from:" + intent.getStringExtra("from"));
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
