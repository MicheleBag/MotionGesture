package com.example.motiongesture;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Vibrator;

public class WaveService extends Service {

        private SensorManager mSensorManager;
        private Sensor mProximity;
        private WaveDetector mWaveDetector;

        private Action action;
        private String waveAction;
        private String wavePackage;

        private Vibrator v;
        private SharedPreferences settings;

        public WaveService() {
        }

        @Override
        public IBinder onBind(Intent intent) {
            // TODO: Return the communication channel to the service.
            throw new UnsupportedOperationException("Not yet implemented");
        }


        @Override
        public void onCreate() {
            super.onCreate();
            v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            action = new Action(this);

            settings = getSharedPreferences("waveSettings", Context.MODE_PRIVATE);
            waveAction = settings.getString("waveAction", "Nessun azione");
            wavePackage = settings.getString("wavePackage", "Null");


            // WaveDetector initialization
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            mWaveDetector = new WaveDetector();
            mWaveDetector.setOnWaveListener(new WaveDetector.OnWaveListener() {
                @Override
                public void onWave() {
                    //v.vibrate(50);
                    action.action(waveAction, wavePackage);
                }
            });
            mSensorManager.registerListener(mWaveDetector, mProximity, SensorManager.SENSOR_DELAY_UI);

        }

        @Override
        public void onDestroy() {
            mSensorManager.unregisterListener(mWaveDetector);
            super.onDestroy();
        }

}

