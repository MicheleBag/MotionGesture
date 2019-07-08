package com.example.motiongesture;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeDetector implements SensorEventListener {

    private long lastTimeShakeDetected = System.currentTimeMillis();
    private long deltaT = 500;
    private float mAccel;
    private float mAccelCurrent = SensorManager.GRAVITY_EARTH;
    private final float threshold = 40f;
    private final float acceleration_multiplier = 0.9f;
    private OnShakeListener mListener;

    public void setOnShakeListener(OnShakeListener listener) {
        this.mListener = listener;
    }

    public interface OnShakeListener {
        void onShake();
    }

    /** Controlla se il modulo del movimento registrato è maggiore della sensibilità stabilità
     *  tenendo conto del precedente shake in modo tale da rendere omogenee le forze.
     *  Effettua anche un controllo temporale sugli intervalli di invocazione*/
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mListener != null) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * acceleration_multiplier + delta;
            //Threshold è la soglia di movimento minimo da superare (sensibility)
            if (mAccel > threshold && System.currentTimeMillis() - lastTimeShakeDetected > deltaT) {
                lastTimeShakeDetected = System.currentTimeMillis();
                mListener.onShake();
            }
        }
    }




    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}





/*
    //mio modo
    private static final float SHAKE_THRESHOLD_GRAVITY = 7F;
    private static final int SHAKE_SLOP_TIME_MS = 500;
    private long mShakeTimestamp;
    */


//MIO METODO
            /*float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;

            // gForce sarà circa 1 quando non ci sono movimenti
            Float f = new Float(gX * gX + gY * gY + gZ * gZ);
            Double d = Math.sqrt(f.doubleValue());
            float gForce = d.floatValue();

            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                final long now = System.currentTimeMillis();
                // ignora shake a troppo frequenti tra loro (500ms)
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                    return;
                }
                mShakeTimestamp = now;
                mListener.onShake();
            }*/
