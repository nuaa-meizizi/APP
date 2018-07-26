package com.scy.health.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.baidu.speech.EventListener;
import com.baidu.speech.asr.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.luseen.luseenbottomnavigation.BottomNavigation.BottomNavigationItem;
import com.luseen.luseenbottomnavigation.BottomNavigation.BottomNavigationView;
import com.luseen.luseenbottomnavigation.BottomNavigation.OnBottomNavigationItemClickListener;
import com.scy.health.Interface.XfyunInterface;
import com.scy.health.LiveService;
import com.scy.health.R;
import com.scy.health.fragment.Driving;
import com.scy.health.fragment.Home;
import com.scy.health.fragment.MySetting;
import com.scy.health.util.BaiduWakeUp;
import com.scy.health.util.PremissionDialog;
import com.scy.health.util.XfyunASR;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;

import static com.scy.health.util.AppUtils.getIMEI;

public class MainActivity extends AppCompatActivity implements EventListener {
    private static final String TAG = "MainActivity";
    private BottomNavigationView bottomNavigationView;
    private String[] titles = {"首页", "驾驶模式", "个人设置"};
    private BaiduWakeUp baiduWakeUp;
    private XfyunASR xfyunASR;
    private Home home;
    private Driving driving;
    private MySetting setting;
    private TextView title;
    private Context context;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        SpeechUtility.createUtility(this, com.iflytek.cloud.SpeechConstant.APPID + "=5ac5d9c8");
        baiduWakeUp = new BaiduWakeUp(this, this);
        xfyunASR = new XfyunASR(this);
        baiduWakeUp.start();
        MultPermission();
        initView();
        setTabSelection(0);
        checkIMEI();
        Intent intent = new Intent(this, LiveService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);
    }

    private void checkIMEI(){
        SharedPreferences sharedPreferences = getSharedPreferences("setting", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (PremissionDialog.lacksPermission("android.permission.READ_PHONE_STATE",context)){
            MultPermission();
        }else {
            String IMEI = getIMEI(context);
            editor.putString("imei",IMEI);
            editor.commit();
        }
    }

    public XfyunASR getXfyunASR(){
        return xfyunASR;
    }

    public BaiduWakeUp getBaiduWakeUp(){
        return baiduWakeUp;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        baiduWakeUp.stop();
        unbindService(conn);
    }

    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        String logTxt = "name: " + name;
        if (params != null && !params.isEmpty()) {
            logTxt += " ;params :" + params;
        } else if (data != null) {
            logTxt += " ;data length=" + data.length;
        }
        if (name.equals(SpeechConstant.CALLBACK_EVENT_WAKEUP_SUCCESS)) {     //唤醒事件
            baiduWakeUp.stop();
            xfyunASR.speekText("请吩咐");
            try {
                Thread.sleep(1200);
            } catch (Exception e) {
                e.printStackTrace();
            }
            xfyunASR.startSpeechDialog(new XfyunInterface() {
                @Override
                public void GetData(String content) {
                    response(content.substring(0, content.length() - 1));
                    baiduWakeUp.start();
                }

                @Override
                public void onError(String errorData) {
                    baiduWakeUp.start();
                }
            });
        }
        baiduWakeUp.printLog(logTxt);
    }

    public void initView() {
        title = (TextView) findViewById(R.id.title);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomview);
        BottomNavigationItem home = new BottomNavigationItem
                ("首页", ContextCompat.getColor(this, R.color.colorAccent), R.drawable.home);
        BottomNavigationItem drive = new BottomNavigationItem
                ("驾驶模式", ContextCompat.getColor(this, R.color.colorAccent), R.drawable.drive);
        BottomNavigationItem setting = new BottomNavigationItem
                ("个人设置", ContextCompat.getColor(this, R.color.colorAccent), R.drawable.people);
        bottomNavigationView.addTab(home);
        bottomNavigationView.addTab(drive);
        bottomNavigationView.addTab(setting);
        bottomNavigationView.isColoredBackground(false);
        bottomNavigationView.setOnBottomNavigationItemClickListener(new OnBottomNavigationItemClickListener() {
            @Override
            public void onNavigationItemClick(int index) {
                setTabSelection(index);
            }
        });
        bottomNavigationView.selectTab(0);
    }

    public void setTabSelection(int index) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch (index) {
            case 0:
                hideFragment(transaction);
                home = new Home();
                transaction.replace(R.id.content, home);
                transaction.commit();
                break;
            case 1:
                hideFragment(transaction);
                driving = new Driving();
                transaction.replace(R.id.content, driving);
                transaction.commit();
                break;
            case 2:

                hideFragment(transaction);
                setting = new MySetting();
                transaction.replace(R.id.content, setting);
                transaction.commit();
                break;
        }
        title.setText(titles[index]);
    }

    private void hideFragment(FragmentTransaction transaction) {
        if (home != null) {
            transaction.remove(home);
        }
        if (driving != null) {
            transaction.remove(driving);
        }
        if (setting != null) {
            transaction.remove(setting);
        }
    }

    public void response(String content) {      //自定义语音指令
        Log.i(TAG, "response: "+content);
        if (content.equals("进入首页") || content.equals("首页")) {
            setTabSelection(0);
            xfyunASR.speekText("好的");
        } else if (content.equals("开车")) {
            setTabSelection(1);
            xfyunASR.speekText("好的");
        } else if (content.equals("个人设置") || content.equals("设置")) {
            setTabSelection(2);
            xfyunASR.speekText("好的");
        } else {
            xfyunASR.speekText("我不能理解你的命令");
        }
    }

    private void MultPermission(){
        RxPermissions rxPermissions = new RxPermissions(MainActivity.this);
        rxPermissions.requestEach(Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION)//权限名称，多个权限之间逗号分隔开
                .subscribe(new Consumer<Permission>(){
                    @Override
                    public void accept(Permission permission){
                        if(permission.name.equals(Manifest.permission.RECORD_AUDIO) && !permission.granted){
                            Log.e("MainActivity","权限被拒绝");
                            PremissionDialog.showMissingPermissionDialog(context,getString(R.string.LACK_RECORD_AUDIO));
                        }
                        if(permission.name.equals(Manifest.permission.CALL_PHONE) && !permission.granted){
                            System.out.println("权限被拒绝");
                            PremissionDialog.showMissingPermissionDialog(context,getString(R.string.LACK_CALL_PHONE));
                        }
                        if(permission.name.equals(Manifest.permission.ACCESS_COARSE_LOCATION) && !permission.granted){
                            System.out.println("权限被拒绝");
                            PremissionDialog.showMissingPermissionDialog(context,getString(R.string.LACK_LOCATION));
                        }
                        if(permission.name.equals(Manifest.permission.READ_PHONE_STATE) && !permission.granted){
                            System.out.println("权限被拒绝");
                            PremissionDialog.showMissingPermissionDialog(context,getString(R.string.LACK_LOCATION));
                        }
                        if(permission.name.equals(Manifest.permission.CAMERA) && !permission.granted){
                            System.out.println("权限被拒绝");
                            PremissionDialog.showMissingPermissionDialog(context,getString(R.string.LACK_CAMERA));
                        }
                        if(permission.name.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && !permission.granted){
                            System.out.println("权限被拒绝");
                            PremissionDialog.showMissingPermissionDialog(context,getString(R.string.LACK_WRITE_EXTERNAL_STORAGE));
                        }
                    }
                });
    }
}
