package com.limvi_licef.ar_driving_assistant.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.aware.Aware;
import com.aware.Aware_Preferences;

import com.limvi_licef.ar_driving_assistant.database.*;
import com.limvi_licef.ar_driving_assistant.R;

import com.limvi_licef.ar_driving_assistant.fragments.UserDialogFragment;
import com.limvi_licef.ar_driving_assistant.receivers.LinearAccelerometerReceiver;
import com.limvi_licef.ar_driving_assistant.receivers.LocationReceiver;
import com.limvi_licef.ar_driving_assistant.receivers.RotationReceiver;
import com.limvi_licef.ar_driving_assistant.receivers.TemperatureReceiver;
import com.limvi_licef.ar_driving_assistant.tasks.ExportTask;
import com.limvi_licef.ar_driving_assistant.utils.Constants;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private final int MONITORING_RESULTS_MAX = 10;

    private DatabaseHelper dbHelper;
    private Intent aware;
    private HandlerThread sensorThread;
    private Handler sensorHandler;
    private HandlerThread rotationThread;
    private Handler rotationHandler;
    private TemperatureReceiver temperatureReceiver;
    private LocationReceiver locationReceiver;
    private LinearAccelerometerReceiver linearAccelerometerReceiver;
    private RotationReceiver rotationReceiver;

    private ArrayList<String> results;
    private ArrayAdapter<String> resultsAdapter;

    IntentFilter statusIntentFilter = new IntentFilter(Constants.ACTION_WRITE_TO_UI);
    private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = (String) intent.getExtras().get(Constants.WRITE_MESSAGE);
            if(results.size() >= MONITORING_RESULTS_MAX) results.clear();
            results.add(status);
            resultsAdapter.notifyDataSetChanged();
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUIElements();
        dbHelper = DatabaseHelper.getHelper(this);
        aware = new Intent(this, Aware.class);
        setupSensors();
        setupListeners();
    }

    @Override
    protected void onDestroy() {
        unregisterListeners();
        stopListenerThreads();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(statusReceiver, statusIntentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(statusReceiver);
    }

    private void setupUIElements() {

        ToggleButton monitoringToggle = (ToggleButton) findViewById(R.id.monitoring_button);
        monitoringToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean toggled) {
                if (toggled) {
                    startMonitoring();

                } else {
                    stopMonitoring();
                }
            }
        });
        Button setupUser = (Button) findViewById(R.id.setup_user_button);
        setupUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserDialogFragment userFragment = UserDialogFragment.newInstance();
                userFragment.show(getFragmentManager(), "usersetupdialog");
            }
        });
        Button exportDatabase = (Button) findViewById(R.id.check_database_button);
        exportDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ExportTask(MainActivity.this).execute();
            }
        });

        ListView resultsView = (ListView) findViewById(R.id.monitoring_result);
        results = new ArrayList<>();
        resultsAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.list_results, results);
        resultsView.setAdapter(resultsAdapter);
        resultsView.setEmptyView(findViewById(R.id.emptyList));
    }

    private void setupSensors(){
        startService(aware);

        Aware.setSetting(this, Aware_Preferences.STATUS_LINEAR_ACCELEROMETER, true);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LINEAR_ACCELEROMETER, 400000);

        //TODO BUG
//        Aware.setSetting(this, Constants.STATUS_FUSED_LOCATION, true, Constants.FUSED_LOCATION_PACKAGE);
//        Aware.setSetting(this, "fallback_location_timeout", 0, Constants.FUSED_LOCATION_PACKAGE);
//        Aware.setSetting(this, Constants.FREQUENCY_FUSED_LOCATION, 0.5, Constants.FUSED_LOCATION_PACKAGE);
//        Aware.setSetting(this, Constants.MAX_FREQUENCY_FUSED_LOCATION, 0, Constants.FUSED_LOCATION_PACKAGE);
//        Aware.setSetting(this, Constants.ACCURACY_FUSED_LOCATION, 100, Constants.FUSED_LOCATION_PACKAGE);
//        Aware.setSetting(this, "location_sensitivity", 0, Constants.FUSED_LOCATION_PACKAGE);

        Aware.setSetting(this, Aware_Preferences.STATUS_LOCATION_GPS, true);
        Aware.setSetting(this, Aware_Preferences.STATUS_LOCATION_NETWORK, false);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LOCATION_GPS, 0);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LOCATION_NETWORK, 1);
        Aware.setSetting(this, Aware_Preferences.MIN_LOCATION_GPS_ACCURACY, 0);
        Aware.setSetting(this, Aware_Preferences.MIN_LOCATION_NETWORK_ACCURACY, 0);
        Aware.setSetting(this, Aware_Preferences.LOCATION_EXPIRATION_TIME, 1);

        Aware.setSetting(this, Constants.STATUS_OPEN_WEATHER, true, Constants.OPEN_WEATHER_PACKAGE);
        Aware.setSetting(this, Constants.FREQUENCY_OPEN_WEATHER, 30, Constants.OPEN_WEATHER_PACKAGE);
        Aware.setSetting(this, Constants.API_KEY_OPEN_WEATHER, getResources().getString(R.string.openweather), Constants.OPEN_WEATHER_PACKAGE);
    }

    private void setupListeners() {
        rotationThread = new HandlerThread("RotationHandlerThread");
        rotationThread.start();
        Looper rotationLooper = rotationThread.getLooper();
        rotationHandler = new Handler(rotationLooper);

        rotationReceiver = new RotationReceiver();

        sensorThread = new HandlerThread("SensorDataHandlerThread");
        sensorThread.start();
        Looper looper = sensorThread.getLooper();
        sensorHandler = new Handler(looper);

        linearAccelerometerReceiver = new LinearAccelerometerReceiver();
        locationReceiver = new LocationReceiver();
        temperatureReceiver = new TemperatureReceiver();
    }

    private void stopListenerThreads() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            rotationThread.quitSafely();
        } else {
            rotationThread.quit();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            sensorThread.quitSafely();
        } else {
            sensorThread.quit();
        }
    }

    private void registerListeners(){
        rotationReceiver.register(this, rotationHandler);
        linearAccelerometerReceiver.register(this, sensorHandler);
        locationReceiver.register(this, sensorHandler);
        temperatureReceiver.register(this, sensorHandler);
    }

    private void unregisterListeners(){
        rotationReceiver.unregister(this);
        linearAccelerometerReceiver.unregister(this);
        locationReceiver.unregister(this);
        temperatureReceiver.unregister(this);

        rotationHandler.removeCallbacksAndMessages(null);
        sensorHandler.removeCallbacksAndMessages(null);
    }

    private void startMonitoring() {
        registerListeners();

        Aware.startLocations(this);
        Aware.startLinearAccelerometer(this);
//        Aware.startPlugin(this, Constants.FUSED_LOCATION_PACKAGE);
        Aware.startPlugin(this, Constants.OPEN_WEATHER_PACKAGE);
    }

    private void stopMonitoring() {
        Aware.stopLocations(this);
        Aware.stopLinearAccelerometer(this);

        unregisterListeners();

        //TODO ??
//        Aware.stopPlugin(this, Constants.FUSED_LOCATION_PACKAGE);
//        Aware.stopPlugin(this, Constants.OPEN_WEATHER_PACKAGE);
    }

}
