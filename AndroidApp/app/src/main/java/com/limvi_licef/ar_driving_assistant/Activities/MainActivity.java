package com.limvi_licef.ar_driving_assistant.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.fragments.SendEventDialogFragment;
import com.limvi_licef.ar_driving_assistant.fragments.SetupDialogFragment;
import com.limvi_licef.ar_driving_assistant.receivers.LinearAccelerometerReceiver;
import com.limvi_licef.ar_driving_assistant.receivers.LocationReceiver;
import com.limvi_licef.ar_driving_assistant.receivers.RotationReceiver;
import com.limvi_licef.ar_driving_assistant.receivers.SensorReceiver;
import com.limvi_licef.ar_driving_assistant.receivers.TemperatureReceiver;
import com.limvi_licef.ar_driving_assistant.tasks.CalibrateTask;
import com.limvi_licef.ar_driving_assistant.tasks.ExportTask;
import com.limvi_licef.ar_driving_assistant.tasks.TrainingTask;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Config;
import com.limvi_licef.ar_driving_assistant.utils.Constants;
import com.limvi_licef.ar_driving_assistant.utils.Events;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private HandlerThread sensorThread;
    private Handler sensorHandler;
    private SensorReceiver temperatureReceiver;
    private SensorReceiver locationReceiver;
    private SensorReceiver linearAccelerometerReceiver;
    private SensorReceiver rotationReceiver;

    private ArrayList<String> results;
    private ArrayAdapter<String> resultsAdapter;

    IntentFilter statusIntentFilter = new IntentFilter(Broadcasts.ACTION_WRITE_TO_UI);
    private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = (String) intent.getExtras().get(Broadcasts.WRITE_MESSAGE);
            results.add(status);
            resultsAdapter.notifyDataSetChanged();
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUIElements();
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

        Button setup = (Button) findViewById(R.id.setup_button);
        setup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetupDialogFragment setupFragment = SetupDialogFragment.newInstance();
                setupFragment.show(getFragmentManager(), "setupdialog");
            }
        });
        Button exportDatabase = (Button) findViewById(R.id.check_database_button);
        exportDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ExportTask(MainActivity.this).execute();
            }
        });
        Button calibrateSensor = (Button) findViewById(R.id.calibrate_sensor_button);
        calibrateSensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CalibrateTask(MainActivity.this).execute();
            }
        });
        Button sendEvent = (Button) findViewById(R.id.send_event_button);
        sendEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendEventDialogFragment eventFragment = SendEventDialogFragment.newInstance();
                eventFragment.show(getFragmentManager(), "eventdialog");
            }
        });
        final ToggleButton trainToggle = (ToggleButton) findViewById(R.id.train_button);
        trainToggle.setEnabled(false);
        trainToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            private long startTimestamp = 0;
            String label;
            String type;
            String message;

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean toggled) {
                long timestamp = System.currentTimeMillis();
                if (toggled) {

                    LinearLayout layout = new LinearLayout(MainActivity.this);
                    layout.setOrientation(LinearLayout.VERTICAL);

                    final EditText labelField = new EditText(MainActivity.this);
                    labelField.setHint(getResources().getString(R.string.training_task_hint_label));
                    layout.addView(labelField);

                    final RadioGroup rg = new RadioGroup(MainActivity.this);
                    rg.setOrientation(RadioGroup.VERTICAL);
                    for(Events.EventTypes event : Events.EventTypes.values()){
                        RadioButton rb = new RadioButton(MainActivity.this);
                        rg.addView(rb);
                        rb.setText(event.name());
                    }
                    rg.check(rg.getChildAt(0).getId());
                    layout.addView(rg);

                    final EditText eventText = new EditText(MainActivity.this);
                    eventText.setHint(getResources().getString(R.string.training_task_hint_message));
                    layout.addView(eventText);

                    new AlertDialog.Builder(MainActivity.this)
                            .setView(layout)
                            .setPositiveButton(getResources().getString(R.string.training_task_dialog_ok), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    startTimestamp = System.currentTimeMillis();
                                    label = labelField.getText().toString();
                                    int index = rg.getCheckedRadioButtonId() % rg.getChildCount();
                                    RadioButton rb = (RadioButton)rg.getChildAt((index == 0) ? rg.getChildCount()-1 : index-1);
                                    type = rb.getText().toString();
                                    message = eventText.getText().toString();
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.training_task_dialog_dismiss), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    trainToggle.setChecked(false);
                                }
                            })
                            .show();
                } else {
                    if(startTimestamp == 0) return;
                    results.clear();
                    linearAccelerometerReceiver.savePrematurely();
                    rotationReceiver.savePrematurely();
                    locationReceiver.savePrematurely();
                    new TrainingTask(new Events.Event(label, startTimestamp, timestamp, Events.EventTypes.valueOf(type), message), MainActivity.this).execute();
                    startTimestamp = 0;
                }
            }
        });

        ToggleButton monitoringToggle = (ToggleButton) findViewById(R.id.monitoring_button);
        monitoringToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean toggled) {
                if (toggled) {
                    startMonitoring();
                    trainToggle.setEnabled(true);

                } else {
                    stopMonitoring();
                    trainToggle.setEnabled(false);
                }
            }
        });

        ListView resultsView = (ListView) findViewById(R.id.monitoring_result);
        results = new ArrayList<>();
        resultsAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.list_results, results);
        resultsView.setAdapter(resultsAdapter);
        resultsView.setEmptyView(findViewById(R.id.emptyList));
    }

    private void setupSensors(){
        Aware.setSetting(this, Aware_Preferences.STATUS_LINEAR_ACCELEROMETER, Config.AwareSettings.ACCELEROMETER_ENABLED);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LINEAR_ACCELEROMETER, Config.AwareSettings.ACCELEROMETER_FREQUENCY);

        //TODO BUG
//        Aware.setSetting(this, Constants.FusedLocationPlugin.STATUS, Config.AwareSettings.FUSED_LOCATION_ENABLED, Constants.FusedLocationPlugin.PACKAGE_NAME);
//        Aware.setSetting(this, Constants.FusedLocationPlugin.FALLBACK_TIMEOUT, Config.AwareSettings.FUSED_LOCATION_FALLBACK_TIMEOUT, Constants.FusedLocationPlugin.PACKAGE_NAME);
//        Aware.setSetting(this, Constants.FusedLocationPlugin.FREQUENCY, Config.AwareSettings.FUSED_LOCATION_FREQUENCY, Constants.FusedLocationPlugin.PACKAGE_NAME);
//        Aware.setSetting(this, Constants.FusedLocationPlugin.MAX_FREQUENCY, Config.AwareSettings.FUSED_LOCATION_MAX_FREQUENCY, Constants.FusedLocationPlugin.PACKAGE_NAME);
//        Aware.setSetting(this, Constants.FusedLocationPlugin.ACCURACY, Config.AwareSettings.FUSED_LOCATION_ACCURACY, Constants.FusedLocationPlugin.PACKAGE_NAME);
//        Aware.setSetting(this, Constants.FusedLocationPlugin.LOCATION_SENSITIVITY, Config.AwareSettings.FUSED_LOCATION_SENSITIVITY, Constants.FusedLocationPlugin.PACKAGE_NAME);

        Aware.setSetting(this, Aware_Preferences.STATUS_LOCATION_NETWORK, Config.AwareSettings.LOCATION_NETWORK_ENABLED);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LOCATION_GPS, Config.AwareSettings.LOCATION_FREQUENCY);
        Aware.setSetting(this, Aware_Preferences.MIN_LOCATION_GPS_ACCURACY, Config.AwareSettings.LOCATION_MIN_GPS_ACCURACY);
        Aware.setSetting(this, Aware_Preferences.LOCATION_EXPIRATION_TIME, Config.AwareSettings.LOCATION_EXPIRATION_TIME);

        Aware.setSetting(this, Constants.OpenWeatherPlugin.STATUS_OPEN_WEATHER, Config.AwareSettings.OPENWEATHER_ENABLED, Constants.OpenWeatherPlugin.OPEN_WEATHER_PACKAGE);
        Aware.setSetting(this, Constants.OpenWeatherPlugin.FREQUENCY_OPEN_WEATHER, Config.AwareSettings.OPENWEATHER_FREQUENCY, Constants.OpenWeatherPlugin.OPEN_WEATHER_PACKAGE);
        Aware.setSetting(this, Constants.OpenWeatherPlugin.API_KEY_OPEN_WEATHER, getResources().getString(R.string.openweather), Constants.OpenWeatherPlugin.OPEN_WEATHER_PACKAGE);
    }

    private void setupListeners() {
        sensorThread = new HandlerThread("SensorDataHandlerThread");
        sensorThread.start();
        Looper looper = sensorThread.getLooper();
        sensorHandler = new Handler(looper);

        rotationReceiver = new RotationReceiver();
        linearAccelerometerReceiver = new LinearAccelerometerReceiver();
        locationReceiver = new LocationReceiver();
        temperatureReceiver = new TemperatureReceiver();
    }

    private void stopListenerThreads() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            sensorThread.quitSafely();
        } else {
            sensorThread.quit();
        }
    }

    private void registerListeners(){
        rotationReceiver.register(this, sensorHandler);
        linearAccelerometerReceiver.register(this, sensorHandler);
        locationReceiver.register(this, sensorHandler);
        temperatureReceiver.register(this, sensorHandler);
    }

    private void unregisterListeners(){
        rotationReceiver.unregister(this);
        linearAccelerometerReceiver.unregister(this);
        locationReceiver.unregister(this);
        temperatureReceiver.unregister(this);
        sensorHandler.removeCallbacksAndMessages(null);
    }

    private void startMonitoring() {
        Aware.setSetting(this, Aware_Preferences.STATUS_LOCATION_GPS, Config.AwareSettings.LOCATION_GPS_ENABLED);

        this.startService(new Intent(this, Aware.class));
        registerListeners();

        Aware.startLocations(this);
        Aware.startLinearAccelerometer(this);
//        Aware.startPlugin(this, Constants.PACKAGE_NAME);
        Aware.startPlugin(this, Constants.OpenWeatherPlugin.OPEN_WEATHER_PACKAGE);
    }

    private void stopMonitoring() {
        Aware.setSetting(this, Aware_Preferences.STATUS_LOCATION_GPS, Config.AwareSettings.LOCATION_GPS_DISABLED);

        this.stopService(new Intent(this, Aware.class));
        unregisterListeners();
        Aware.stopAWARE();
//        Aware.stopPlugin(this, Constants.PACKAGE_NAME);
        Aware.stopPlugin(this, Constants.OpenWeatherPlugin.OPEN_WEATHER_PACKAGE);
    }

}
