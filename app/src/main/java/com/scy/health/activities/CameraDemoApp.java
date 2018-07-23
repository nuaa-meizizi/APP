package com.scy.health.activities;

import android.app.Application;

import com.awen.camera.CameraApplication;

public class CameraDemoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CameraApplication.init(this,true);
    }
}
