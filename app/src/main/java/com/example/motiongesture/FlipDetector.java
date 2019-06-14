package com.example.motiongesture;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;


public class FlipDetector implements SensorEventListener {

    private OnFlipListener mListener;
    private int eventOccurred;

    public void setOnFlipListener(OnFlipListener listener) {
        this.mListener = listener;

        this.eventOccurred = 0;
    }

    public interface OnFlipListener {
        void onFaceUp();
        void onFaceDown();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ignora
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (mListener != null) {
            float z = event.values[2];
            if (z > 9 && z < 10 && eventOccurred != 1) {
                eventOccurred = 1;
                mListener.onFaceUp();
            } else if (z > -10 && z < -9 && eventOccurred != 2) {
                eventOccurred = 2;
                mListener.onFaceDown();
            }

        }
    }
}

