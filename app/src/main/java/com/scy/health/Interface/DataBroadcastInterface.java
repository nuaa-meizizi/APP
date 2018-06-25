package com.scy.health.Interface;

public interface DataBroadcastInterface {
    void onaTemperatureChanged(float temperature);
    void onHeartbeatChanged(int heartbeat);
    void onBpChanged(int[] bp);
    void onChanged(float temperature,int heartbeat,int[] bp);
}
