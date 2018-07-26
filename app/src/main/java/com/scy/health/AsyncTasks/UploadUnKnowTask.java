package com.scy.health.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class UploadUnKnowTask extends AsyncTask<Void, Void, Boolean> {
    private Context context;
    private SweetAlertDialog dialog;
    private String path;
    private String uploadUrl = "http://192.168.254.63:8080/upload_unknown";

    public UploadUnKnowTask(Context context, SweetAlertDialog dialog, String path) {
        this.context = context;
        this.dialog = dialog;
        this.path = path;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
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
            dos.writeBytes("Content-Disposition: form-data; name=\"fileField\"; filename=\"+path+\"" + end);
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
            //接收结果
            InputStream is = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String result = br.readLine();
            dos.close();
            is.close();
        }catch (Exception e) {
            return null;
        }
        return null;
    }
}
