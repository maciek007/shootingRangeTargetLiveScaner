package com.example.shootingrangelivescanner;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.JavaCameraView;

public class CustomCameraView extends JavaCameraView {
    private static final String TAG = "CustomCameraView";

    public CustomCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomCameraView(Context context, int cameraId) {
        super(context, cameraId);
    }

    public void setZoom(float zoomPercentage) {
        synchronized (this) {
            if (mCamera == null) return;
            try {
                Camera.Parameters params = mCamera.getParameters();
                if (params.isZoomSupported()) {
                    int maxZoom = params.getMaxZoom();
                    int zoomLevel = Math.round(zoomPercentage * maxZoom);
                    zoomLevel = Math.max(0, Math.min(zoomLevel, maxZoom));
                    params.setZoom(zoomLevel);
                    mCamera.setParameters(params);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting zoom: " + e.getMessage());
            }
        }
    }

    public boolean isZoomSupported() {
        synchronized (this) {
            if (mCamera == null) return false;
            try {
                Camera.Parameters params = mCamera.getParameters();
                return params.isZoomSupported();
            } catch (Exception e) {
                return false;
            }
        }
    }

    public int getMaxZoom() {
        synchronized (this) {
            if (mCamera == null) return 0;
            try {
                Camera.Parameters params = mCamera.getParameters();
                return params.isZoomSupported() ? params.getMaxZoom() : 0;
            } catch (Exception e) {
                return 0;
            }
        }
    }
}
