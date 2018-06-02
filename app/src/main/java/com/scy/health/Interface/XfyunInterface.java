package com.scy.health.Interface;

public interface XfyunInterface {
    //语音识别回调
    void GetData(String content);
    void onError(String errorData);
}
