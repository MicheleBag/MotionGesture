package com.example.motiongesture;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Vibrator;


public class PickUpService extends Service {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private PickUpDetector mPickUpDetector;

    private Action action;
    private String pickupAction;
    private String pickupPackage;

    private Vibrator v;
    private SharedPreferences settings;

    public PickUpService() {
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

        settings = getSharedPreferences("pickupSettings", Context.MODE_PRIVATE);
        pickupAction = settings.getString("pickupAction", "Nessun azione");
        pickupPackage = settings.getString("pickupPackage", "Null");


        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mPickUpDetector = new PickUpDetector();
        mPickUpDetector.setOnPickUpListener(new PickUpDetector.OnPickUpListener() {
            @Override
            public void onPickUp() {
                //v.vibrate(50);
                action.action(pickupAction, pickupPackage);
            }
        },this);
        mSensorManager.registerListener(mPickUpDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(mPickUpDetector);
        super.onDestroy();
    }

}