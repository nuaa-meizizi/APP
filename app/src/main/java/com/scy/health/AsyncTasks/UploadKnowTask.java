package com.scy.health.AsyncTasks;

import android.content.Context;
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

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.scy.health.util.AppUtils.getPicName;

public class UploadKnowTask extends AsyncTask<Void, Void, Boolean> {
    private final String TAG = "UploadUnKnowTask";
    private Context context;
    private SweetAlertDialog dialog;
    private String path;
    private String uploadUrl = "http://192.168.254.63:8080/upload_known";
    private String getPathUrl = "http://app.logicjake.xyz:8080/health/settings/getpath";

    public UploadKnowTask(Context context, SweetAlertDialog dialog, String path) {
        this.context = context;
        this.dialog = dialog;
        this.path = path;
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
                    uploadUrl = res.getString("data") + "upload_known";
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
        String boundary = "******";
        String end = "\r\n";
        String twoHyphens = "--";
        try {
            URL uri = new URL(uploadUrl);
            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
            connection.setRequestMethod("POST");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(10000);
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
            String picName = getPicName(path);
            dos.writeBytes("Content-Disposition: form-data; name=\"fileField\"; filename=\""+picName+"\"" + end);
            dos.writeBytes(end);

            FileInputStream fis = new FileInputStream(path);
            byte[] buffer = new byte[8192]; // 8k
            int count = 0;
            while ((count = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, count);

            }
            fis.close();
            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            dos.flush();
            int responseCode = connection.getResponseCode();// 调用此方法就不必再使用conn.connect()方法
            if (responseCode == 200) {
                //接收结果
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String line;
                StringBuilder sb = new StringBuilder();
                while ( ( line = br.readLine() ) != null ) {
                    sb.append(line);
                }
                Log.d(TAG, "doInBackground: "+sb.toString());
                is.close();
            } else {
                Log.e(TAG, "doInBackground: "+responseCode);
                return false;
            }
            dos.close();
        }catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result){
        if (result){
            Toast.makeText(context,"保存成功",Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context,"保存人脸失败",Toast.LENGTH_SHORT).show();
        }
        dialog.cancel();
    }
}
