package com.example.motiongesture;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.PowerManager;

import static android.content.Context.POWER_SERVICE;

/**Gestisce le azioni possibili richiamabili dalle gesture*/

public class Action {

    boolean flashstate;
    Context c;

    public Action(Context c)
    {
        this.c = c;
        flashstate = false;
    }

    public void action(String text, String pack) {

        switch (text) {
            case "Google Assistant":
                c.startActivity(new Intent(Intent.ACTION_VOICE_COMMAND)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;

            case "Do not disturb on":
                setNonDisturabare(true);
                break;

            case "Do not disturb off":
                setNonDisturabare(false);
                break;

            case "Wake up screen":
                PowerManager.WakeLock screenLock = ((PowerManager) c.getSystemService(POWER_SERVICE)).newWakeLock(
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "MotionGesture:wakelocktag");
                screenLock.acquire();
                screenLock.release();
                break;

            case "No action":
                break;

            case "Flashlight":

                CameraManager camManager = (CameraManager) c.getSystemService(Context.CAMERA_SERVICE);
                String cameraId = null; // Usually back camera is at 0 position.

                try {
                    cameraId = camManager.getCameraIdList()[0];
                    flashstate = !flashstate;
                    camManager.setTorchMode(cameraId, flashstate);

                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }

                break;

            default:
                Intent launchIntent = c.getPackageManager().getLaunchIntentForPackage(pack);
                //launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                if (launchIntent != null) {
                    c.startActivity(launchIntent);  //null pointer check in case package name was not found
                }
        }
    }


        public void setNonDisturabare ( boolean on)
        {

            NotificationManager mNotificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mNotificationManager.isNotificationPolicyAccessGranted() == false) {
                Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                c.startActivity(intent);
            }

            if (on) {
                if (mNotificationManager.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_ALARMS) {
                    mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS);
                }
            } else if (!on) {
                if (mNotificationManager.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_ALL) {
                    mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
                }
            }

        }
    }