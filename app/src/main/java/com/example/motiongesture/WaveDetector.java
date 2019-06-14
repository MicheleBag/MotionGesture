package com.example.motiongesture;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class WaveDetector implements SensorEventListener {

    private long lastProximityEventTime = 0;
    private int lastProximityState;
    private final float threshold = 1000;

    private com.example.motiongesture.WaveDetector.OnWaveListener mListener;

    public void setOnWaveListener(com.example.motiongesture.WaveDetector.OnWaveListener listener) {
        this.mListener = listener;
    }

    public interface OnWaveListener {
        void onWave();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ignora
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mListener != null) {
            float distance = event.values[0];
            int proximityState;
            /*
            The Proximity far.
            */
            int proximityFar = 0;
            /*
            The Proximity near.
            */
            int proximityNear = 1;
            if (distance == 0)
            {
                proximityState = proximityNear;
            }
            else
            {
                proximityState = proximityFar;
            }

            final long now = System.currentTimeMillis();
            final long eventDeltaMillis = now - this.lastProximityEventTime;
            if (eventDeltaMillis < threshold
                    && proximityNear == lastProximityState
                    && proximityFar == proximityState)
            {
                    // Wave detected
                    mListener.onWave();
            }
            this.lastProximityEventTime = now;
            this.lastProximityState = proximityState;
            }
        }
}


