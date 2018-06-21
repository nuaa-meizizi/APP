package com.scy.health.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.scy.health.util.BlueTooth;

import java.io.IOException;
import java.util.Random;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.scy.health.util.SharedPreferencesDataBase.insert;


public class getBlueToothDataTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "getBlueToothDataTask";
    private Boolean ready = false;
    private Context context;
    private BlueTooth blueTooth;
    private String res = null;
    private int count;
    private SweetAlertDialog sweetAlertDialog;

    public getBlueToothDataTask(Context context, SweetAlertDialog sweetAlertDialog)
    {
        this.context = context;
        this.sweetAlertDialog = sweetAlertDialog;
    }

    @Override
    protected String doInBackground(String... strings) {
//        if (isCancelled()){
//            cancel();
//            return null;
//        }
//        blueTooth = new BlueTooth(context);
//        blueTooth.start(new BluetoothInterface() {
//            @Override
//            public void onSuccess() {
//                Log.i(TAG, "onSuccess: 蓝牙连接成功");
//            }
//
//            @Override
//            public void onError(String errorData) {
//                Log.e(TAG, "onError: "+errorData);
//            }
//
//            @Override
//            public void onReceive(String data) {
//                System.out.println("----------------------------:"+data);
//                handleData(data);
//            }
//        });
//        while (!ready){
//            if (isCancelled()){
//                cancel();
//                return null;
//            }
//        }
//        cancel();
        return res;
    }

    public void handleData(String data){
        count++;
        if (count == 10) {
            res = data;
            ready = true;       //数据处理完毕
        }
    }

    @Override
    protected void onPostExecute(String result) {
//        if (res == null)
//            Log.e(TAG, "onPostExecute: res返回空值" );
//        if (isCancelled()){
//            cancel();
//            return;
//        }
        insert(context,new Random().nextInt(40-36)+36,new Random().nextInt(85-70)+70);            //新添一条体检记录
        sweetAlertDialog.cancel();
    }

    public void cancel(){
        Log.i(TAG, "cancel: 执行了cancel方法");
        if (blueTooth != null) {
            try {
                blueTooth.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
