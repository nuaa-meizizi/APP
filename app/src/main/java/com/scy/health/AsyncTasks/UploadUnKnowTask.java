package com.scy.health.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.provider.Telephony.Mms.Part.CHARSET;
import static com.scy.health.util.AppUtils.getPicName;

public class UploadUnKnowTask extends AsyncTask<Void, Void, Boolean> {
    private final String TAG = "UploadUnKnowTask";
    private Context context;
    private SweetAlertDialog dialog;
    private String path;
    private String uploadUrl = "http://192.168.254.63:8080/upload_unknown";
    private String getPathUrl = "http://app.logicjake.xyz:8080/health/settings/getpath";
    private String getTokenUrl = "http://app.logicjake.xyz:8080/health/user/getToken";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public UploadUnKnowTask(Context context, SweetAlertDialog dialog, String path) {
        this.context = context;
        this.dialog = dialog;
        this.path = path;
        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        //先获取上传地址
        StringBuilder sb = new StringBuilder();
        InputStream is = null;
        BufferedReader br = null;
        try {
            //接口地址
            URL uri = new URL(getPathUrl);
            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(10000);
            connection.setRequestProperty("accept", "*/*");

            int responseCode = connection.getResponseCode();// 调用此方法就不必再使用conn.connect()方法
            if (responseCode == 200) {
                //接收结果
                is = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                //缓冲逐行读取
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                Log.d(TAG, "doInBackground() returned: " + sb.toString());
                JSONObject res = new JSONObject(sb.toString());
                if (res.getInt("status") == 0) {
                    uploadUrl = res.getString("data") + "upload_unknown";
                    return uploadPic();
                }
            }
            else
                return false;
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
        return false;
    }

    private boolean uploadPic(){
        String boundary = UUID.randomUUID().toString();
        String end = "\r\n";
        String twoHyphens = "--";
        try {
            URL uri = new URL(uploadUrl);
            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
            connection.setRequestMethod("POST");
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(10000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("connection", "keep-alive");
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
            String picName = getPicName(path);
            StringBuffer sb = new StringBuffer();
            sb.append(twoHyphens);
            sb.append(boundary);
            sb.append(end);
            sb.append("Content-Disposition: form-data; name=\"fileField\"; filename=\""
                    + picName + "\"" + end);
            sb.append("Content-Type: application/octet-stream; charset="
                    + CHARSET + end);
            sb.append(end);
            dos.write(sb.toString().getBytes());

            FileInputStream fis = new FileInputStream(path);
            byte[] buffer = new byte[1024]; // 8k
            int count = 0;
            while ((count = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, count);
            }
            fis.close();
            dos.write(end.getBytes());
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            dos.flush();
            int responseCode = connection.getResponseCode();// 调用此方法就不必再使用conn.connect()方法
            Log.d(TAG, "uploadPic: "+responseCode);
            if (responseCode == 200) {
                //接收结果
                InputStream input = connection.getInputStream();
                StringBuffer sb1 = new StringBuffer();
                int ss;
                while ((ss = input.read()) != -1) {
                    sb1.append((char) ss);
                }
                Log.d(TAG, "doInBackground: "+sb1.toString());
                JSONObject res = new JSONObject(sb1.toString());
                if (res.getInt("code") == 0) {
                    return getToken(res.getString("data"));
                }
            } else {
                Log.e(TAG, "doInBackground: "+responseCode);
                return false;
            }
            dos.close();
        }catch (Exception e) {
            Log.e(TAG, "uploadPic: ", e);
            return false;
        }
        return false;
    }

    private boolean getToken(String uid){
        StringBuilder sb = new StringBuilder();
        InputStream is = null;
        BufferedReader br = null;
        try {
            //接口地址
            URL uri = new URL(getTokenUrl+"?"+uid);
            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(10000);
            connection.setRequestProperty("accept", "*/*");

            int responseCode = connection.getResponseCode();// 调用此方法就不必再使用conn.connect()方法
            if (responseCode == 200) {
                //接收结果
                is = connection.getInputStream();
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                //缓冲逐行读取
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                Log.d(TAG, "doInBackground() returned: " + sb.toString());
                JSONObject res = new JSONObject(sb.toString());
                if (res.getInt("status") == 0) {
                    editor.putString("token",res.getJSONObject("data").getString("token"));
                    editor.commit();
                    return true;
                }
                else
                    return false;
            }
            else
                return false;
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
        return false;
    }
    @Override
    protected void onPostExecute(Boolean result){
        if (result){
            ((Activity)context).finish();
        }else {
            Toast.makeText(context,"验证人脸失败",Toast.LENGTH_SHORT).show();
        }
        dialog.cancel();
    }
}
