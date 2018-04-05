package com.scy.health.util;
import android.content.Context;
import android.util.Log;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;


public class BaiduWakeUp {
    private EventManager wakeup;
    private boolean logTime = true;
    private static final String TAG = "BaiduWakeUp";

    public void start() {
        Map<String, Object> params = new TreeMap<String, Object>();

        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        params.put(SpeechConstant.WP_WORDS_FILE, "assets:///WakeUp.bin");
        // "assets:///WakeUp.bin" 表示WakeUp.bin文件定义在assets目录下
        String json = new JSONObject(params).toString();
        wakeup.send(SpeechConstant.WAKEUP_START, json, null, 0, 0);
    }

    public void stop() {
        wakeup.send(SpeechConstant.WAKEUP_STOP, null, null, 0, 0); //
    }

    public BaiduWakeUp(Context context, EventListener eventListener){
        wakeup = EventManagerFactory.create(context, "wp");
        wakeup.registerListener(eventListener); //  EventListener 中 onEvent方法
    }

    public void printLog(String text) {
        if (logTime) {
            text += "  ;time=" + System.currentTimeMillis();
        }
        text += "\n";
        Log.i(TAG, text);
    }
}
