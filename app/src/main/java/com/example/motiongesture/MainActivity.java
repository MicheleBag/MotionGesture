package com.example.motiongesture;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
DA FARE:
 -Testare ads
 -Detector e service delle gesture:
        -proximity *da decidere se riaggiungere o meno*
        -ottimizzare wristtwist ---> da testare
        -abbassare sensibility pickup device ---> da testare

 -Tutorial iniziale ---> better design
 -Sensibility from the user

    *PLAY STORE*
    *VERISONE PRO*
        - acquisti in-app   ------> Partita iva?
        - check licenza valida -----> Google Firebase?

ULTIMA COSA FATTA:
    -Redirect alla recensione
    -Check permessi restrizioni background all'avvio e allo start dei services
    -Added ads interstitial after enabling shake service
*/


public class MainActivity extends AppCompatActivity {

    public ArrayList <String> nameList;
    public ArrayList <String> packageList;
    public ArrayList <String> sortedNameList;

    public Spinner shakeSpinner;
    public Switch shakeSwitch;
    public String shakeAction;

    public Spinner flipUpSpinner;
    public Switch flipupSwitch;
    public String flipupAction;

    public Spinner flipDownSpinner;
    public Switch flipdownSwitch;
    public String flipdownAction;

    public Spinner wristTwistSpinner;
    public Switch wristTwistSwitch;
    public String wristTwistAction;

    /*
    //Proximity
    public Spinner proximitySpinner;
    */

    public Spinner waveSpinner;
    public Switch waveSwitch;
    public String waveAction;

    public Spinner pickupSpinner;
    public Switch pickupSwitch;
    public String pickupAction;

    SharedPreferences settings;

    View contentView;
    int shortAnimationDuration;

    boolean bgPermission = false; //true if no background restrictions

    private InterstitialAd interstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Rater app (Play store)
        AppRater.app_launched(this);

        //Spinner
        shakeSpinner = findViewById(R.id.spinner_shake);
        flipUpSpinner = findViewById(R.id.spinner_flipup);
        flipDownSpinner = findViewById(R.id.spinner_flipdown);
        wristTwistSpinner = findViewById(R.id.spinner_wristTwist);
            //proximitySpinner = findViewById(R.id.spinner_proximity);
        waveSpinner = findViewById(R.id.spinner_wave);
        pickupSpinner = findViewById(R.id.spinner_pickup);

        //Switch
        shakeSwitch = findViewById(R.id.switch_shake);
        flipupSwitch = findViewById(R.id.switch_flipup);
        flipdownSwitch = findViewById(R.id.switch_flipdown);
        wristTwistSwitch = findViewById(R.id.switch_wristTwist);
        waveSwitch = findViewById(R.id.switch_wave);
        pickupSwitch = findViewById(R.id.switch_pickup);

        nameList= new ArrayList<>();    /**contiene il nome delle app installate*/
        packageList= new ArrayList<>(); /**contiene il package name delle app
                                          installate per un eventuale startActivity()*/


        /**Controlla le restrizioni in background*/
        bgPermission = checkBatteryRestrictions();

        getInstalledApps(); /** Aggiungo allo spinner le app installate nel device*/
        checkActiveServices(); /**Check iniziale dei servizi attivi*/

        //GESTIONE SENSORE SHAKE
        shakeManager();

        //GESTIONE SENSORE FLIP UP
        flipUpManager();

        // GESTIONE SENSORE FLIP DOWN
        flipDownManager();


        //GESTIONE SENSORE WRIST TWIST
        wristTwistManager();

        /*
        //GESTIONE SENSORE PROXIMITY
        Switch proximitySwitch = findViewById(R.id.switch_proximity);
        proximitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on){
                if(on)
                {
                    //Do something when Switch button is on/checked
                    Sensey.getInstance().startProximityDetection(proximityListener);
                }
                else
                {
                    //Do something when Switch is off/unchecked
                    Sensey.getInstance().stopProximityDetection(proximityListener);

                }
            }
        });
        */

        //GESTIONE SENSORE WAVE
        waveManager();

        // GESTIONE SENSORE PICK UP
        pickupManager();


        /**TUTORIAL*/
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.activity_tutorial, null);
        // Initially hide the content view.
        contentView.setVisibility(View.GONE);
        // Retrieve and cache the system's default "short" animation time.
        shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);


        /**ADS Interstitial*/
        // Prepare the Interstitial Ad
        interstitialAd = new InterstitialAd(MainActivity.this);
        // Insert the Ad Unit ID
        interstitialAd.setAdUnitId(getString(R.string.admob_interstitial_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());
        // Prepare an Interstitial Ad Listener
        interstitialAd.setAdListener(new AdListener()
        {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });
    }

    public void displayInterstitial()
    {
        /*settings = getSharedPreferences("Ads", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        int adsCount = settings.getInt("Ads", 0);*/

        // If Interstitial Ads are loaded then show else show nothing.
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }


    /**Aggiunge le impostazioni della gesture selezionata alle preferenze in modo da
     essere utilizzate dai servizi */
    public void addSettings(String gesture, String action)
    {
        settings = getSharedPreferences(gesture+"Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(gesture+"Action", action);
        editor.commit();
        int index = nameList.lastIndexOf(action);
        if(index > 5)
        {
            String pack = packageList.get(index);
            editor.putString(gesture+"Package", pack);
            editor.commit();
        }
        index = sortedNameList.lastIndexOf(action);
        editor.putInt("spinnerIndex", index);
        editor.commit();
    }

    /**Controlla se ci sono le limitazioni in background e apre una finestra invocando showBatteryPermissions()
     * returns true se non ci sono limitazioni*/
    public boolean checkBatteryRestrictions()
    {
        /**Rimuove le restrizioni in background*/
        //Controlla se ha le limitazioni in background
        String myPackage = this.getPackageName();
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(myPackage)) {
            showBatteryPermissions(this);
        }
        return pm.isIgnoringBatteryOptimizations(myPackage);
    }

    /**Apre una dialog per chiedere i permessi delle limits in background*/
    public void showBatteryPermissions(final Context mContext) {

        final Dialog dialog = new Dialog(mContext);
        dialog.setTitle("Background restrictions");
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
        TextView hey = new TextView(mContext);
        hey.setText("Hey!");
        hey.setTextSize(24);
        hey.setTextColor(Color.BLACK);
        hey.setWidth(240);
        hey.setGravity(Gravity.CENTER);
        hey.setPadding(40, 10, 40, 0);
        ll.addView(hey);

        //Text
        TextView tv = new TextView(mContext);
        tv.setText("MotionGesture needs your permission to ignore the limitations in background to check your gesture when the app is closed\n");
        tv.setTextSize(20);
        tv.setTextColor(Color.BLACK);
        tv.setWidth(240);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(40, 0, 40, 0);
        ll.addView(tv);

        //Give perms button params
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(420, 160);
        params.setMargins(0,0,0, 20);

        //Give perms button
        Button b1 = new Button(mContext);
        b1.setText("Okay");
        b1.setTextSize(16);
        b1.setGravity(Gravity.CENTER);
        b1.setPadding(0,0,0,5);
        b1.setBackgroundResource(R.drawable.rateapp_button);
        b1.setLayoutParams(params);
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                mContext.startActivity(intent);
                dialog.dismiss();
            }
        });
        ll.addView(b1);

        //Exit button parameters
        params = new LinearLayout.LayoutParams(350, 80);
        params.setMargins(0,0,0,20);

        //Button3
        Button b3 = new Button(mContext);
        b3.setText("Exit");
        b3.setTextSize(11);
        b3.setGravity(Gravity.CENTER);
        b3.setPadding(0,5,0,5);
        b3.setBackgroundResource(R.drawable.rateapp_littlebutton);
        b3.setLayoutParams(params);
        b3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Close app
                finish();
                System.exit(0);
                dialog.dismiss();
            }
        });
        ll.addView(b3);


        dialog.setContentView(ll);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    /**Controlla permessi non disturbare*/
    public boolean checkDndPermission(String action)
    {
        if(action == "Do not disturb off" || action == "Do not disturb on")
        {
            NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mNotificationManager.isNotificationPolicyAccessGranted() == false) {
                Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                this.startActivity(intent);
                return false;
            }
        }
        return true;
    }

    /**Controlla i cambiamenti dello switch di ogni gesture e ne attiva/disattiva il servizio*/
    public void shakeManager()
    {
        shakeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on) {
                Intent intent = new Intent(MainActivity.this, ShakeService.class);
                if (on) {
                    //Do something when Switch button is on/checked
                    shakeAction = shakeSpinner.getSelectedItem().toString();
                    if (!checkDndPermission(shakeAction) || !bgPermission) {
                        shakeSwitch.setChecked(false);
                        bgPermission = checkBatteryRestrictions();
                        return;
                    }
                    shakeSpinner.setEnabled(false);
                    addSettings("shake", shakeAction);
                    startService(intent);

                    //ADS
                    displayInterstitial();
                } else {
                    //Do something when Switch is off/unchecked
                    shakeSpinner.setEnabled(true);
                    stopService(intent);
                }


            }
        });
    }

    /**Controlla i servizi attivi e ne ripristina le impostazioni*/
    public void checkActiveServices()
    {
        //Shake service
        if(isMyServiceRunning(ShakeService.class))
        {
            shakeSwitch.setChecked(true);
            shakeSpinner.setEnabled(false);
            settings = getSharedPreferences("shakeSettings", Context.MODE_PRIVATE);
            shakeSpinner.setSelection(settings.getInt("spinnerIndex", 0));
        }
        //FlipUp service
        if(isMyServiceRunning(FlipUpService.class))
        {
            flipupSwitch.setChecked(true);
            flipUpSpinner.setEnabled(false);
            settings = getSharedPreferences("flipupSettings", Context.MODE_PRIVATE);
            flipUpSpinner.setSelection(settings.getInt("spinnerIndex", 0));
        }
        //FlipDown service
        if(isMyServiceRunning(FlipDownService.class)) {
            flipdownSwitch.setChecked(true);
            flipDownSpinner.setEnabled(false);
            settings = getSharedPreferences("flipdownSettings", Context.MODE_PRIVATE);
            flipDownSpinner.setSelection(settings.getInt("spinnerIndex", 0));
        }

        //Wave service
        if(isMyServiceRunning(WaveService.class))
        {
            waveSwitch.setChecked(true);
            waveSpinner.setEnabled(false);
            settings = getSharedPreferences("waveSettings", Context.MODE_PRIVATE);
            waveSpinner.setSelection(settings.getInt("spinnerIndex", 0));
        }
        //PickUp service
        if(isMyServiceRunning(PickUpService.class))
        {
            pickupSwitch.setChecked(true);
            pickupSpinner.setEnabled(false);
            settings = getSharedPreferences("pickupSettings", Context.MODE_PRIVATE);
            pickupSpinner.setSelection(settings.getInt("spinnerIndex", 0));
        }
    }

    public void flipUpManager()
    {
        flipupSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on){
                Intent intent = new Intent(MainActivity.this, FlipUpService.class);
                if(on)
                {
                    //Do something when Switch button is on/checked
                    flipupAction = flipUpSpinner.getSelectedItem().toString();
                    if(!checkDndPermission(flipupAction) || !bgPermission)
                    {
                        flipupSwitch.setChecked(false);
                        bgPermission = checkBatteryRestrictions();
                        return;
                    }
                    flipUpSpinner.setEnabled(false);
                    addSettings("flipup", flipupAction);
                    startService(intent);
                }
                else
                {
                    //Do something when Switch is off/unchecked
                    flipUpSpinner.setEnabled(true);
                    stopService(intent);
                }
            }
        });
    }

    public void flipDownManager()
    {
        flipdownSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on){
                Intent intent = new Intent(MainActivity.this, FlipDownService.class);
                if(on)
                {
                    //Do something when Switch button is on/checked
                    flipdownAction = flipDownSpinner.getSelectedItem().toString();
                    if(!checkDndPermission(flipdownAction) || !bgPermission)
                    {
                        flipdownSwitch.setChecked(false);
                        bgPermission = checkBatteryRestrictions();
                        return;
                    }
                    flipDownSpinner.setEnabled(false);
                    addSettings("flipdown", flipdownAction);
                    startService(intent);
                }
                else
                {
                    //Do something when Switch is off/unchecked
                    flipDownSpinner.setEnabled(true);
                    stopService(intent);
                }
            }
        });
    }

    public void waveManager()
    {
        waveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on){
                Intent intent = new Intent(MainActivity.this, WaveService.class);
                if(on)
                {
                    //Do something when Switch button is on/checked
                    waveAction = waveSpinner.getSelectedItem().toString();
                    if(!checkDndPermission(waveAction) || !bgPermission)
                    {
                        waveSwitch.setChecked(false);
                        bgPermission = checkBatteryRestrictions();
                        return;
                    }
                    waveSpinner.setEnabled(false);
                    addSettings("wave", waveAction);
                    startService(intent);
                }
                else
                {
                    //Do something when Switch is off/unchecked
                    waveSpinner.setEnabled(true);
                    stopService(intent);
                }
            }
        });
    }

    public void pickupManager()
    {
        pickupSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on){
                Intent intent = new Intent(MainActivity.this, PickUpService.class);
                if(on)
                {
                    //Do something when Switch button is on/checked
                    pickupAction = pickupSpinner.getSelectedItem().toString();
                    if(!checkDndPermission(pickupAction) || !bgPermission)
                    {
                        pickupSwitch.setChecked(false);
                        bgPermission = checkBatteryRestrictions();
                        return;
                    }
                    pickupSpinner.setEnabled(false);
                    addSettings("pickup", pickupAction);
                    startService(intent);
                }
                else
                {
                    //Do something when Switch is off/unchecked
                    pickupSpinner.setEnabled(true);
                    stopService(intent);
                }
            }
        });
    }

    public void wristTwistManager()
    {
        wristTwistSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on){
                Intent intent = new Intent(MainActivity.this, WristTwistService.class);
                if(on)
                {
                    //Do something when Switch button is on/checked
                    wristTwistAction = wristTwistSpinner.getSelectedItem().toString();
                    if(!checkDndPermission(wristTwistAction) || !bgPermission)
                    {
                        wristTwistSwitch.setChecked(false);
                        bgPermission = checkBatteryRestrictions();
                        return;
                    }
                    wristTwistSpinner.setEnabled(false);
                    addSettings("wristTwist", wristTwistAction);
                    startService(intent);
                }
                else
                {
                    //Do something when Switch is off/unchecked
                    wristTwistSpinner.setEnabled(true);
                    stopService(intent);
                }
            }
        });
    }

    /*
    //RILEVAMENTI GESTURE
    ProximityDetector.ProximityListener proximityListener=new ProximityDetector.ProximityListener() {
        @Override public void onNear() {
            // Near to device
            v.vibrate(50);
            String text = proximitySpinner.getSelectedItem().toString();
            action(text);

        }
        @Override public void onFar() {
            // Far from device
        }
    };
    */

    /**Lista le app installate nel device*/
    public void getInstalledApps()
    {
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> list = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        //AGGIUNGO APP ALLA LISTA
        for (ApplicationInfo packageInfo : list) {
            if((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1)
            {
                nameList.add("" + packageInfo.loadLabel(getPackageManager()).toString());
                packageList.add("" + packageInfo.packageName);
            }
        }

        //ORDINO UNA LISTA SECONDARIA DI NOME IN ORDINE ALFABETICO
        sortedNameList = new ArrayList<>();
        for (int k = 0; k < nameList.size(); k++)
        {
            sortedNameList.add(nameList.get(k));
        }
        Collections.sort(sortedNameList, new Comparator<String>()
        {
            @Override
            public int compare(String text1, String text2)
            {

                return text1.compareToIgnoreCase(text2);
            }
        });

        //AGGIUNGO ALLA LISTA CHE ANDRA' NELLO SPINNER LE AZIONI CHE NON DIPENDONO DALLE APP INSTALLATE
        sortedNameList.add(0, "No action");
        sortedNameList.add(1, "Do not disturb on");
        sortedNameList.add(2, "Do not disturb off");
        sortedNameList.add(3, "Wake up screen");
        sortedNameList.add(4, "Flashlight");
        sortedNameList.add(5, "Google Assistant");


        //AGGIUNGO APP ALLO SPINNER
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>( MainActivity.this,
                android.R.layout.simple_spinner_item, sortedNameList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shakeSpinner.setAdapter(dataAdapter);
        flipUpSpinner.setAdapter(dataAdapter);
        flipDownSpinner.setAdapter(dataAdapter);
        wristTwistSpinner.setAdapter(dataAdapter);
            //proximitySpinner.setAdapter(dataAdapter);
        waveSpinner.setAdapter(dataAdapter);
        pickupSpinner.setAdapter(dataAdapter);

    }

    public void openTutorial(View view) {
        // Imposto il content view visibile ma trasparente, cosi da andare ad animare il crossfade
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(contentView, width, height, focusable);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        contentView.setAlpha(0f);
        contentView.setVisibility(View.VISIBLE);

        // Animo la view fino al 100% della trasparenza
        contentView.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration);

        // Rimuovo la finestra pop-up quando si clicca fuori
        contentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                //Toast.makeText(this, "servizio run", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        //Toast.makeText(this, "Servizio giu", Toast.LENGTH_SHORT).show();
        return false;
    }
}



