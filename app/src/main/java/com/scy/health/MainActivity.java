package com.scy.health;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.baidu.speech.EventListener;
import com.baidu.speech.asr.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.luseen.luseenbottomnavigation.BottomNavigation.BottomNavigationItem;
import com.luseen.luseenbottomnavigation.BottomNavigation.BottomNavigationView;
import com.luseen.luseenbottomnavigation.BottomNavigation.OnBottomNavigationItemClickListener;
import com.scy.health.fragment.Driving;
import com.scy.health.fragment.Home;
import com.scy.health.fragment.MySetting;
import com.scy.health.util.BaiduWakeUp;
import com.scy.health.util.XfyunASR;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements EventListener {
    private BottomNavigationView bottomNavigationView;
    private String[] titles = {"首页","驾驶模式","个人设置"};
    private BaiduWakeUp baiduWakeUp;
    private XfyunASR xfyunASR;
    private Home home;
    private Driving driving;
    private MySetting setting;
    private TextView title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SpeechUtility.createUtility(this, com.iflytek.cloud.SpeechConstant.APPID +"=5ac5d9c8");
        baiduWakeUp = new BaiduWakeUp(this,this);
        xfyunASR = new XfyunASR(this);
        baiduWakeUp.start();
        //底部选择栏
        initView();
        setTabSelection(0);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        baiduWakeUp.stop();
    }

    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        String logTxt = "name: " + name;
        if (params != null && !params.isEmpty()) {
            logTxt += " ;params :" + params;
        }
        else if (data != null) {
            logTxt += " ;data length=" + data.length;
        }
        if (name.equals(SpeechConstant.CALLBACK_EVENT_WAKEUP_SUCCESS)){     //唤醒事件
            baiduWakeUp.stop();
            xfyunASR.speekText("请吩咐");
            try {
                Thread.sleep(1200);
            } catch (Exception e) {
                e.printStackTrace();
            }
            xfyunASR.startSpeechDialog(baiduWakeUp);
        }
        baiduWakeUp.printLog(logTxt);
    }

    public void initView(){
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottomview);
        BottomNavigationItem home = new BottomNavigationItem
                ("首页",ContextCompat.getColor(this, R.color.colorAccent), R.drawable.home);
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

        title = (TextView)findViewById(R.id.title);
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
}
