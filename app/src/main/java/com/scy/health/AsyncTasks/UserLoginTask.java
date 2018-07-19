package com.scy.health.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.scy.health.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

    private final String mEmail;
    private final String mPassword;
    String loginUrl = "http://app.logicjake.xyz:8080/health/user/login";
    private Context context;
    private EditText mEmailView;
    private EditText mPasswordView;
    private Button mEmailSignInButton;
    private SweetAlertDialog dialog;
    private static final String TAG = "UserLoginTask";

    public UserLoginTask(Context context, SweetAlertDialog dialog,Button mEmailSignInButton, EditText mEmailView, EditText mPasswordView, String email, String password) {
        this.context = context;
        this.mEmailView = mEmailView;
        this.dialog = dialog;
        this.mEmailSignInButton = mEmailSignInButton;
        this.mPasswordView = mPasswordView;;
        mEmail = email;
        mPassword = password;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
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
            if (res.getInt("status") == 0)
                return true;
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        mEmailSignInButton.setClickable(true);
        dialog.cancel();
        if (success) {
            ((Activity)context).finish();
        } else {
            mPasswordView.setError(context.getString(R.string.error_incorrect_password));
            mPasswordView.requestFocus();
        }
    }

    @Override
    protected void onCancelled() {
        dialog.cancel();
        mEmailSignInButton.setClickable(true);
    }
}
