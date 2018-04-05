package com.scy.health;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import com.baidu.speech.EventListener;
import com.baidu.speech.asr.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.scy.health.util.BaiduWakeUp;
import com.scy.health.util.XfyunASR;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements EventListener {

    private BaiduWakeUp baiduWakeUp;
    private XfyunASR xfyunASR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SpeechUtility.createUtility(this, com.iflytek.cloud.SpeechConstant.APPID +"=5ac5d9c8");
        baiduWakeUp = new BaiduWakeUp(this,this);
        xfyunASR = new XfyunASR(this);
        baiduWakeUp.start();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        baiduWakeUp.stop();
    }

    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        String logTxt = "name: " + name;
        System.out.println(name);
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
}
