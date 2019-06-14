package com.example.motiongesture;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Vibrator;

public class FlipDownService extends Service {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private FlipDetector mFlipDownDetector;

    private Action action;
    private String flipdownAction;
    private String flipdownPackage;

    private Vibrator v;
    private SharedPreferences settings;

    public FlipDownService() {
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

        settings = getSharedPreferences("flipdownSettings", Context.MODE_PRIVATE);
        flipdownAction = settings.getString("flipdownAction", "Nessun azione");
        flipdownPackage = settings.getString("flipdownPackage", "Null");


        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mFlipDownDetector = new FlipDetector();
        mFlipDownDetector.setOnFlipListener(new FlipDetector.OnFlipListener() {
            @Override
            public void onFaceUp() {
                //Ignora in quanto è il servizio down

            }

            @Override
            public void onFaceDown(){
                //v.vibrate(50);

                //Log.d("Action", shakeAction);
                //Log.d("Package", shakePackage);

                action.action(flipdownAction, flipdownPackage);
            }

        });
        mSensorManager.registerListener(mFlipDownDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(mFlipDownDetector);
        super.onDestroy();
    }

}


