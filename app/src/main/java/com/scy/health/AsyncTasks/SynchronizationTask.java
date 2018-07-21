package com.scy.health.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SynchronizationTask extends AsyncTask<String, Void, JSONObject> {
    private static final String TAG = "SynchronizationTask";
    String synchUrl = "http://app.logicjake.xyz:8080/health/sensor/synchronization";
    private Context context;
    private JSONObject localdata;
    private ImageView sprogress;
    private ProgressBar progress;
    private String token;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    public SynchronizationTask(Context context, JSONObject localdata, ImageView sprogress, ProgressBar progress) {
        this.context = context;
        this.sprogress = sprogress;
        this.progress = progress;
        this.localdata = localdata;
        sp = ((Activity)context).getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sp.edit();
        this.token = sp.getString("token",null);
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        StringBuilder sb = new StringBuilder();
        InputStream is = null;
        BufferedReader br = null;
        try {
            //接口地址
            URL uri = new URL(synchUrl+"?token="+token);
            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(10000);
            connection.setRequestProperty("accept", "*/*");
            //发送参数
            connection.setDoOutput(true);
            PrintWriter out = new PrintWriter(connection.getOutputStream());
            out.print(localdata);
            out.flush();
            //接收结果
            is = connection.getInputStream();
            br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            //缓冲逐行读取
            while ( ( line = br.readLine() ) != null ) {
                sb.append(line);
            }
            Log.d(TAG, "doInBackground() returned: " + sb.toString());
            return new JSONObject(sb.toString());
        } catch ( Exception ignored ) {
        } finally {
            //关闭流
            try {
                if(is!=null){
                    is.close();
                }
                if(br!=null){
                    br.close();
                }
            } catch ( Exception ignored ) {
                Log.e(TAG, "doInBackground: ",ignored );
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        sprogress.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        try {
            if (result.getInt("status") == 130004){
                Toast.makeText(context,"登陆信息有误或过期",Toast.LENGTH_SHORT).show();
                editor.remove("token").commit();            //清除token数据
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
