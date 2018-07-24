package com.awen.camera;

import android.hardware.Camera;

public interface MyFaceDetectionListener {
    void onFaceDetection(Camera.Face[] faces, Camera camera);
}
