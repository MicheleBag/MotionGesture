package com.example.motiongesture;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Vibrator;


public class WristTwistService extends Service {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private WristTwistDetector mWristTwistDetector;

    private Action action;
    private String wristTwistAction;
    private String wristTwistPackage;

    private Vibrator v;
    private SharedPreferences settings;

    public WristTwistService() {
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

        settings = getSharedPreferences("wristTwistSettings", Context.MODE_PRIVATE);
        wristTwistAction = settings.getString("wristTwistAction", "Nessun azione");
        wristTwistPackage = settings.getString("wristTwistPackage", "Null");


        // WristTwistDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mWristTwistDetector = new WristTwistDetector();
        mWristTwistDetector.setOnWristTwistListener(new WristTwistDetector.OnWristTwistListener() {
            @Override
            public void onWristTwist() {
                v.vibrate(50);

                //Log.d("Action", wristTwistAction);
                //Log.d("Package", wristTwistPackage);

                action.action(wristTwistAction, wristTwistPackage);
            }
        });
        mSensorManager.registerListener(mWristTwistDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(mWristTwistDetector);
        super.onDestroy();
    }

}
