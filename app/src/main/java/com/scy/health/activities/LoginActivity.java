package com.scy.health.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.awen.camera.view.TakePhotoActivity;
import com.scy.health.AsyncTasks.SignUpTask;
import com.scy.health.AsyncTasks.UploadKnowTask;
import com.scy.health.AsyncTasks.UploadUnKnowTask;
import com.scy.health.AsyncTasks.UserLoginTask;
import com.scy.health.R;
import com.scy.health.util.PremissionDialog;
import com.tbruyelle.rxpermissions2.RxPermissions;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.reactivex.functions.Consumer;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private UserLoginTask mAuthTask = null;
    private SweetAlertDialog dialog;
    private EditText mEmailView;
    private EditText mPasswordView;
    private Button mEmailSignInButton,faceLoginButton;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private TextView changeFunction;
    final SpannableStringBuilder style = new SpannableStringBuilder();
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        changeFunction = (TextView)findViewById(R.id.toRegister);
        faceLoginButton = (Button)findViewById(R.id.face_button);
        sharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        context = this;
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLoginOrSignup();
                    return true;
                }
                return false;
            }
        });

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLoginOrSignup();
            }
        });

        faceLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PremissionDialog.lacksPermission("android.permission.CAMERA",context)){
                    getPermission();
                }else {
                    Intent intent = new Intent(context,TakePhotoActivity.class);
                    startActivityForResult(intent, TakePhotoActivity.REQUEST_LOGIN_CODE);
                }
            }
        });

        toLoginAction();
    }

    private void toRegisterAction(){
        setTitle("注册");
        mPasswordView.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);              //明文
        mEmailSignInButton.setText("注册");
        style.clear();
        style.append("返回登陆");
        //设置部分文字点击事件
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                toLoginAction();
            }
        };
        style.setSpan(clickableSpan, 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        changeFunction.setText(style);
        //设置部分文字颜色
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.BLUE);
        style.setSpan(foregroundColorSpan, 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        changeFunction.setMovementMethod(LinkMovementMethod.getInstance());
        changeFunction.setText(style);
    }

    private void toLoginAction(){
        setTitle("登陆");
        mPasswordView.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);              //明文
        mEmailSignInButton.setText("登陆");
        style.clear();
        style.append("现在注册");
        //设置部分文字点击事件
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                toRegisterAction();
            }
        };
        style.setSpan(clickableSpan, 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        changeFunction.setText(style);
        //设置部分文字颜色
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.BLUE);
        style.setSpan(foregroundColorSpan, 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        changeFunction.setMovementMethod(LinkMovementMethod.getInstance());
        changeFunction.setText(style);
    }

    private void attemptLoginOrSignup() {
        dialog = new SweetAlertDialog(LoginActivity.this,SweetAlertDialog.PROGRESS_TYPE);
        dialog.setCancelable(false);
        dialog.show();
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(email)) {
            dialog.cancel();
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }
        else if (TextUtils.isEmpty(password)) {
            dialog.cancel();
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mEmailSignInButton.setClickable(false);
            if (mEmailSignInButton.getText().toString().equals("登陆")){
                mAuthTask = new UserLoginTask(this,dialog,mEmailSignInButton,mEmailView,mPasswordView,email, password);
                mAuthTask.execute((Void) null);
            }
            else if (mEmailSignInButton.getText().toString().equals("注册")) {
                new SignUpTask(this, dialog, mEmailSignInButton, mEmailView, mPasswordView, email, password).execute((Void) null);
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TakePhotoActivity.REQUEST_CAPTRUE_CODE: {
                    String path = data.getStringExtra(TakePhotoActivity.RESULT_PHOTO_PATH);
                    Log.v(TAG, "REQUEST_CAPTRUE_CODE：" + path);
                    dialog.show();
                    new UploadKnowTask(this, dialog, path).execute((Void) null);
                    //注册提交图片
                    break;
                }
                case TakePhotoActivity.REQUEST_LOGIN_CODE:{
                    String path = data.getStringExtra(TakePhotoActivity.RESULT_PHOTO_PATH);
                    Log.v(TAG, "REQUEST_LOGIN_CODE：" + path);
                    dialog.show();
                    new UploadUnKnowTask(this, dialog, path).execute((Void) null);
                    //登录提交图片
                    break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getPermission(){
        RxPermissions rxPermissions = new RxPermissions(this); // where this is an Activity instance
        rxPermissions.request(Manifest.permission.CAMERA)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        if (granted) { // 在android 6.0之前会默认返回true
                            Intent intent = new Intent(context,TakePhotoActivity.class);
                            startActivityForResult(intent, TakePhotoActivity.REQUEST_LOGIN_CODE);
                        } else {
                            PremissionDialog.showMissingPermissionDialog(context,getString(R.string.LACK_CAMERA));
                        }
                    }
                });
    }
}

