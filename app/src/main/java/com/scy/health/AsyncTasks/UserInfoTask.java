package com.scy.health.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UserInfoTask extends AsyncTask<String, Void, JSONObject> {
    private static final String TAG = "UserInfoTask";
    String userInfoSynchUrl = "http://app.logicjake.xyz:8080/health/info/synchinfo";
    private Context context;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String token;
    private String sex;
    private String province;
    private String phone;
    private TextView sexView,phoneView;

    public UserInfoTask(Context context, TextView sexView,TextView phoneView) {
        this.context = context;
        this.sexView = sexView;
        this.phoneView = phoneView;
        sp = ((Activity)context).getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sp.edit();
        token = sp.getString("token",null);
        province = sp.getString("province","no_exist");
        phone = sp.getString("phone","no_exist");
        sex = sp.getString("sex","no_exist");
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        StringBuilder sb = new StringBuilder();
        InputStream is = null;
        BufferedReader br = null;
        try {
            //接口地址
            String param = "?token="+token+"&province="+province+"&phone="+phone+"&sex="+sex;
            URL uri = new URL(userInfoSynchUrl+param);
            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(10000);
            connection.setRequestProperty("accept", "*/*");
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
    protected void onPostExecute(JSONObject result){
        if (result!=null){
            try {
                String sex = result.getJSONObject("data").getString("sex");
                String phone = result.getJSONObject("data").getString("phone");
                if (!sex.equals("null") && sex.length()!=0){
                    editor.putString("sex",sex);
                    editor.commit();
                }
                if (!phone.equals("null") && phone.length()!=0){
                    editor.putString("phone",phone);
                    editor.commit();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sexView.setText(sp.getString("sex","未设置"));
            phoneView.setText(sp.getString("phone","未设置"));
        }else
            Toast.makeText(context,"同步用户信息失败",Toast.LENGTH_SHORT).show();
    }
}
