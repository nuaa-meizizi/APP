package com.scy.health.AsyncTasks;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.awen.camera.view.TakePhotoActivity;
import com.scy.health.R;
import com.scy.health.util.PremissionDialog;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.reactivex.functions.Consumer;

public class SignUpTask extends AsyncTask<Void, Void, JSONObject> {
    private final String mEmail;
    private final String mPassword;
    String loginUrl = "http://app.logicjake.xyz:8080/health/user/signup";
    private Context context;
    private EditText mEmailView;
    private EditText mPasswordView;
    private Button mEmailSignInButton;
    private SweetAlertDialog dialog;
    private SweetAlertDialog mdialog;
    private static final String TAG = "SignUpTask";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Intent intent;

    public SignUpTask(Context context, SweetAlertDialog dialog,Button mEmailSignInButton, EditText mEmailView, EditText mPasswordView, String email, String password) {
        this.context = context;
        this.mEmailView = mEmailView;
        this.dialog = dialog;
        this.mEmailSignInButton = mEmailSignInButton;
        this.mPasswordView = mPasswordView;;
        mEmail = email;
        mPassword = password;
        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        try {
            String param = "name="+mEmail+"&password="+mPassword;
            StringBuilder sb = new StringBuilder();
            InputStream is = null;
            BufferedReader br = null;
            URL uri = new URL(loginUrl);
            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
            connection.setRequestMethod("POST");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(10000);
            connection.setRequestProperty("accept", "*/*");
            //发送参数
            connection.setDoOutput(true);
            PrintWriter out = new PrintWriter(connection.getOutputStream());
            out.print(param);
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
            JSONObject res = new JSONObject(sb.toString());
            return  res;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(final JSONObject res) {
        if (res != null) {
            try {
                dialog.cancel();
                if (res.getInt("status") == 0) {
                    editor.putString("name", mEmail);
                    editor.putString("password", mPassword);
                    editor.putString("token", res.getJSONObject("data").getString("token"));
                    editor.commit();
                    mdialog = new SweetAlertDialog(context,SweetAlertDialog.NORMAL_TYPE);
                    mdialog.setTitle("是否录入人脸");
                    mdialog.setContentText("下次可刷脸登录");
                    mdialog.setCancelText("算了");
                    mdialog.setConfirmText("好啊");
                    mdialog.showCancelButton(true);
                    mdialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            mdialog.cancel();
                            ((Activity) context).finish();
                        }
                    });
                    mdialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            if (mdialog!=null)
                                mdialog.cancel();
                            try {
                                if (PremissionDialog.lacksPermission("android.permission.CAMERA",context)){
                                    getPermission();
                                }else {
                                    intent = new Intent(context, TakePhotoActivity.class);
                                    intent.putExtra("isSignUp",true);
                                    intent.putExtra("uid",res.getJSONObject("data").getString("id"));
                                    ((Activity) context).startActivityForResult(intent, TakePhotoActivity.REQUEST_CAPTRUE_CODE);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                    mdialog.show();
                }
                else if (res.getInt("status") == 130000){
                    mEmailView.setError(context.getString(R.string.error_user_existence));
                    mEmailView.requestFocus();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            mEmailSignInButton.setClickable(true);
        }
    }

    @Override
    protected void onCancelled() {
        dialog.cancel();
        mEmailSignInButton.setClickable(true);
    }

    private void getPermission() {
        RxPermissions rxPermissions = new RxPermissions((Activity) context); // where this is an Activity instance
        rxPermissions.request(Manifest.permission.CAMERA)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        if (granted) { // 在android 6.0之前会默认返回true
                            ((Activity) context).startActivityForResult(intent, TakePhotoActivity.REQUEST_CAPTRUE_CODE);
                        } else {
                            PremissionDialog.showMissingPermissionDialog(context, context.getString(R.string.LACK_CAMERA));
                        }
                    }
                });
    }
}
