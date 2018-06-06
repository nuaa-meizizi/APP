package com.scy.health.Interface;

public interface BluetoothInterface {
    void onSuccess();
    void onError(String errorData);
    void onReceive(String data);
}
