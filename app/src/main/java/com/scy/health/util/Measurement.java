package com.scy.health.util;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Measurement{
    private static final String TAG = "Measurement";

    /*
        根据指标返回string字符串，监测结果
        bp[0]:收缩压
        bp[1]:舒张压
         */
    private String res;

    public static String measureIndicator(float temperature, int heartbeat, int[] bp, String sex){
        String res = "";
        if (temperature > 37)
            res+="体温较正常人偏高\n";
        else if (temperature < 36)
            res+="体温较正常人偏低\n";
        if (heartbeat < 60)
            res+="心率较正常人偏高\n";
        else if (heartbeat > 100)
            res+="心率较正常人偏高\n";
        if (bp[0]>=140 || bp[1]>=90)
            res+="血压偏高\n";
        if (bp[0]<90 || bp[1]<60)
            res+="血压偏低\n";
        if (res.length() < 2)
            res = "指标正常，您的身体很健康";
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        String args = Float.toString(temperature)+" "+Integer.toString(heartbeat)+" 60 "+Integer.toString(bp[0])+" "+Integer.toString(bp[1]);
        Log.i(TAG, "driveMeasureIndicator: "+args);
        formBody.add("args",args);//传递键值对参数
        RequestBody body = formBody.build();
        Request request = new Request.Builder()
                .post(body)//传递请求体
                .url("https://car2018.nuaa.app/?service=App.Car.Health_predict")
                .build();
        OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象。
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "解析失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    String rses = response.body().string();
                    Log.i(TAG, "onResponse: "+rses);
                    try {
                        JSONObject jsonObject = new JSONObject(rses);
                        String data = jsonObject.getString("data");
                        if(data.contains("0")){
                            //不正常
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        return res;
    }

    public String driveMeasureIndicator(float temperature, int heartbeat, int[] bp, double[] eye, String sex){
        res = "";
        if (temperature > 37)
            res+="体温较正常人偏高\n";
        else if (temperature < 36)
            res+="体温较正常人偏低\n";
        if (heartbeat < 60)
            res+="心率较正常人偏高\n";
        else if (heartbeat > 100)
            res+="心率较正常人偏高\n";
        if (bp[0]>=140 || bp[1]>=90)
            res+="血压偏高\n";
        if (bp[0]<90 || bp[1]<60)
            res+="血压偏低\n";
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        String args = "";
        for(int i = 0; i < eye.length;i++)
            args = args+Double.toString(eye[i])+" ";
        Log.i(TAG, "driveMeasureIndicator: "+args);
        formBody.add("args",args);//传递键值对参数
        RequestBody body = formBody.build();
        Request request = new Request.Builder()
                .post(body)//传递请求体
                .url("https://car2018.nuaa.app/?service=App.Car.Eye_predict")
                .build();
        OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象。
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "解析失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response != null && response.isSuccessful()) {
                    String rses = response.body().string();
                    Log.i(TAG, "onResponse: "+rses);
                    try {
                        JSONObject jsonObject = new JSONObject(rses);
                        String data = jsonObject.getString("data");
                        if(data.contains("-1")){
                            res+="疲劳驾驶\n";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        Log.i(TAG, "driveMeasureIndicator: "+res);
        return "疲劳驾驶\n";
    }
}
