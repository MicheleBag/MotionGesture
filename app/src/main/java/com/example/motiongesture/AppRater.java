package com.example.motiongesture;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Use AppRater.showRateDialog(this, null) to test on myActivity
 *  use AppRater.app_launched(this) in the normal use calling it from mainActivity, onCreate()
 */

public class AppRater {
    private final static String APP_TITLE = "MotionGesture";
    private final static String APP_PNAME = "com.example.motiongesture";

    private final static int DAYS_UNTIL_PROMPT = 2;
    private final static int LAUNCHES_UNTIL_PROMPT = 5;

    public static void app_launched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) { return ; }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening dialog
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(mContext, editor);
            }
        }

        editor.commit();
    }

    public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {

        final Dialog dialog = new Dialog(mContext);
        dialog.setTitle("Rate " + APP_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        //Popup windows
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = 800;
        lp.height = 900;

        //Layout
        LinearLayout ll = new LinearLayout(mContext);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);
        ll.setBackgroundResource(R.drawable.tutorial_background);

        //Text
        TextView tv = new TextView(mContext);
        tv.setText("If you enjoy using " + APP_TITLE + ", please take a moment to rate it. Thanks for your support!");
        tv.setTextSize(20);
        tv.setTextColor(Color.BLACK);
        tv.setWidth(240);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(40, 20, 40, 80);
        ll.addView(tv);

        //Rate button params
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(420, 160);
        params.setMargins(0,0,0, 20);

        //Rate button
        Button b1 = new Button(mContext);
        b1.setText("Rate " + APP_TITLE);
        b1.setTextSize(16);
        b1.setGravity(Gravity.CENTER);
        b1.setPadding(0,5,0,5);
        b1.setBackgroundResource(R.drawable.rateapp_button);
        b1.setLayoutParams(params);
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                dialog.dismiss();
            }
        });
        ll.addView(b1);

        //2-3 buttons parameters
        params = new LinearLayout.LayoutParams(350, 80);
        params.setMargins(0,0,0,20);

        //Button2
        Button b2 = new Button(mContext);
        b2.setText("Remind me later");
        b2.setTextSize(11);
        b2.setGravity(Gravity.CENTER);
        b2.setPadding(0,5,0,5);
        b2.setBackgroundResource(R.drawable.rateapp_littlebutton);
        b2.setLayoutParams(params);
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ll.addView(b2);

        //Button3
        Button b3 = new Button(mContext);
        b3.setText("No, thanks");
        b3.setTextSize(11);
        b3.setGravity(Gravity.CENTER);
        b3.setPadding(0,5,0,5);
        b3.setBackgroundResource(R.drawable.rateapp_littlebutton);
        b3.setLayoutParams(params);
        b3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });
        ll.addView(b3);


        dialog.setContentView(ll);
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

}
