package com.example.motiongesture;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Vibrator;

public class FlipUpService extends Service {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private FlipDetector mFlipUpDetector;

    private boolean firstStart = true;
    private Action action;
    private String flipupAction;
    private String flipupPackage;

    private Vibrator v;
    private SharedPreferences settings;

    public FlipUpService() {
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

        settings = getSharedPreferences("flipupSettings", Context.MODE_PRIVATE);
        flipupAction = settings.getString("flipupAction", "Nessun azione");
        flipupPackage = settings.getString("flipupPackage", "Null");


        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mFlipUpDetector = new FlipDetector();
        mFlipUpDetector.setOnFlipListener(new FlipDetector.OnFlipListener() {
            @Override
            public void onFaceUp() {
                if(!firstStart)
                {
                    //v.vibrate(50);

                    //Log.d("Action", shakeAction);
                    //Log.d("Package", shakePackage);

                    action.action(flipupAction, flipupPackage);
                }
                firstStart = false;
            }

            @Override
            public void onFaceDown(){
                //Ignora in quanto è il servizio up
            }

        });
        mSensorManager.registerListener(mFlipUpDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(mFlipUpDetector);
        super.onDestroy();
    }

}

