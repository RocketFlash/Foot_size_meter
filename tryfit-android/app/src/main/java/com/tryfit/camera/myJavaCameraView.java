package com.tryfit.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

/**
 * Created by Rauf Yagfarov on 02/10/2017.
 */

public class myJavaCameraView extends JavaCameraView {

    public myJavaCameraView(Context context, int cameraId) {
        super(context, cameraId);
    }

    public myJavaCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void turnOffTheFlash() {
        Camera.Parameters params = mCamera.getParameters();
        params.setFlashMode(params.FLASH_MODE_OFF);
        mCamera.setParameters(params);
    }

    public void turnOnTheFlash() {
        Camera.Parameters params = mCamera.getParameters();
        params.setFlashMode(params.FLASH_MODE_TORCH);
        mCamera.setParameters(params);
    }
}
