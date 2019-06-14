package com.example.motiongesture;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.display.DisplayManager;
import android.view.Display;

public class PickUpDetector implements SensorEventListener {

    private OnPickUpListener mListener;

    private long lastTimeGestureDetected = System.currentTimeMillis();
    private long lastTimeInclinationCheck = System.currentTimeMillis();
    private final int threshold = 11;
    private final long deltaGesture = 1500;
    private final long deltaInclination = 1500;
    private int inclination;
    private int count = 0;
    private boolean deviceStill = false;
    private Context context;

    public void setOnPickUpListener(OnPickUpListener listener, Context context) {
        this.mListener = listener;
        this.context = context;

    }

    public interface OnPickUpListener {
        public void onPickUp();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ignora
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (mListener != null) {
            //Pick up movement detection
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double vectorSum = Math.sqrt(x * x + y * y + z * z);
            double zNormalized = z / vectorSum;


            /**Aggiorno l'inclinazione ogni delta sec e controllo se il device è "fermo", quindi
             * aggiorno il contatore e se il device è fermo da 2*delta sec allora viene dichiarato
             * fermo ufficialmente e rende possibile richiamare la funzione, altrimenti azzera il
             * count e dichiara il telefono non fermo
             */
            if(System.currentTimeMillis() - lastTimeInclinationCheck > deltaInclination)
            {
                inclination = (int) Math.round(Math.toDegrees(Math.acos(zNormalized)));
                lastTimeInclinationCheck = System.currentTimeMillis();
                if(inclination > 155 || inclination < 25 && !checkScreenOn())
                    count++;
                else {
                    count = 0;
                    deviceStill = false;
                }
                if(count == 2)
                    deviceStill = true;
            }

            /**Controllo se il device è fermo, controllo se il movimento è maggiore della soglia,
             * controllo la temporizzazione in modo da non ripetere più .onPickUp() con lo stesso
             * richiamo della gesture, a quel punto aggiorno il timer e chiamo la funzione
             */
            if(deviceStill)
            {
                if (vectorSum > threshold) {
                    if(System.currentTimeMillis() - lastTimeGestureDetected > deltaGesture)
                    {
                        lastTimeGestureDetected = System.currentTimeMillis();
                        mListener.onPickUp();
                    }
                }
            }


        }
    }

    /**Controlla se lo schermo è acceso, torna true se acceso, false se spento*/
    public boolean checkScreenOn()
    {
        DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        for (Display display : dm.getDisplays()) {
            if (display.getState() != Display.STATE_OFF) {
                return true;
            }
        }
        return false;
    }
}

