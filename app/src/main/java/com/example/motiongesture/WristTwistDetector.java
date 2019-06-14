package com.example.motiongesture;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class WristTwistDetector implements SensorEventListener {

    private boolean isGestureInProgress = false;
    private long lastTimeWristTwistDetected = System.currentTimeMillis();
    private final float threshold = 6f;
    private final long timeForWristTwistGesture = 1000;


    private OnWristTwistListener mListener;

    public void setOnWristTwistListener(OnWristTwistListener listener) {
        this.mListener = listener;
    }

    public interface OnWristTwistListener {
        public void onWristTwist();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ignora
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (mListener != null) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            // Make this higher or lower according to how much
            // motion you want to detect
            if (x < -9.8f && y > -3f && z < (-threshold)) {
                lastTimeWristTwistDetected = System.currentTimeMillis();
                isGestureInProgress = true;
            } else {
                long timeDelta = (System.currentTimeMillis() - lastTimeWristTwistDetected);
                if (timeDelta > timeForWristTwistGesture && isGestureInProgress) {
                    isGestureInProgress = false;
                    mListener.onWristTwist();
                }
            }
        }
    }

}