package com.example.motiongesture;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Vibrator;


public class ShakeService extends Service {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    private Action action;
    private String shakeAction;
    private String shakePackage;

    private Vibrator v;
    private SharedPreferences settings;

    public ShakeService() {
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

        settings = getSharedPreferences("shakeSettings", Context.MODE_PRIVATE);
        shakeAction = settings.getString("shakeAction", "Nessun azione");
        shakePackage = settings.getString("shakePackage", "Null");


        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake() {
                    v.vibrate(50);

                    //Log.d("Action", shakeAction);
                    //Log.d("Package", shakePackage);

                    action.action(shakeAction, shakePackage);
            }
        });
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(mShakeDetector);
        super.onDestroy();
    }

}
