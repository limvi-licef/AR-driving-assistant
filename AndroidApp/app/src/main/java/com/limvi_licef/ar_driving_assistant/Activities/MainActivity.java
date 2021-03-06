package com.limvi_licef.ar_driving_assistant.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.config.AwareSettings;
import com.limvi_licef.ar_driving_assistant.fragments.CreateTrainingEventDialogFragment;
import com.limvi_licef.ar_driving_assistant.fragments.DeleteEventDialogFragment;
import com.limvi_licef.ar_driving_assistant.fragments.ListEventsDialogFragment;
import com.limvi_licef.ar_driving_assistant.fragments.SendEventDialogFragment;
import com.limvi_licef.ar_driving_assistant.fragments.SetupDTWDialogFragment;
import com.limvi_licef.ar_driving_assistant.fragments.SetupDialogFragment;
import com.limvi_licef.ar_driving_assistant.models.Event;
import com.limvi_licef.ar_driving_assistant.receivers.LinearAccelerometerReceiver;
import com.limvi_licef.ar_driving_assistant.receivers.LocationReceiver;
import com.limvi_licef.ar_driving_assistant.receivers.RotationReceiver;
import com.limvi_licef.ar_driving_assistant.receivers.SensorReceiver;
import com.limvi_licef.ar_driving_assistant.receivers.TemperatureReceiver;
import com.limvi_licef.ar_driving_assistant.runnables.MatchEventRunnable;
import com.limvi_licef.ar_driving_assistant.tasks.CalibrateTask;
import com.limvi_licef.ar_driving_assistant.tasks.ExportTask;
import com.limvi_licef.ar_driving_assistant.tasks.TrainingTask;
import com.limvi_licef.ar_driving_assistant.network.TCPListenerThread;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements  View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private HandlerThread sensorThread;
    private Handler sensorHandler;
    private HandlerThread dtwThread;
    private Handler dtwHandler;
    private SensorReceiver temperatureReceiver;
    private SensorReceiver locationReceiver;
    private SensorReceiver linearAccelerometerReceiver;
    private SensorReceiver rotationReceiver;

    private ArrayList<String> results;
    private ArrayAdapter<String> resultsAdapter;

    public ToggleButton trainToggle;
    public ToggleButton monitoringToggle;

    private TCPListenerThread tcpListenerThread = null;

    private long startTimestamp = 0;
    private String label;
    private String type;
    private String message;

    /**
     * Writes incoming messages to UI
     */
    IntentFilter statusIntentFilter = new IntentFilter(Broadcasts.ACTION_WRITE_TO_UI);
    private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = (String) intent.getExtras().get(Broadcasts.WRITE_MESSAGE);
            results.add(status);
            resultsAdapter.notifyDataSetChanged();
    }};

    /**
     * Starts DTW algorithm
     */
    IntentFilter dtwIntentFilter = new IntentFilter(Broadcasts.ACTION_START_DTW);
    private final BroadcastReceiver dtwReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dtwHandler.post(new MatchEventRunnable(MainActivity.this));
        }};

    /**
     * Called when a button has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setup_button :
                SetupDialogFragment setupFragment = SetupDialogFragment.newInstance();
                setupFragment.show(getSupportFragmentManager(), "setupdialog");
                break;
            case R.id.check_database_button :
                new ExportTask(MainActivity.this).execute();
                break;
            case R.id.calibrate_sensor_button :
                new CalibrateTask(MainActivity.this).execute();
                break;
            case R.id.send_event_button :
                SendEventDialogFragment eventFragment = SendEventDialogFragment.newInstance();
                eventFragment.show(getSupportFragmentManager(), "eventdialog");
                break;
            case R.id.setup_dtw_button :
                SetupDTWDialogFragment dtwFragment = SetupDTWDialogFragment.newInstance();
                dtwFragment.show(getSupportFragmentManager(), "dtwdialog");
                break;
            case R.id.list_events_button:
                ListEventsDialogFragment deleteFragment = ListEventsDialogFragment.newInstance();
                deleteFragment.show(getSupportFragmentManager(), "listeventsdialog");
                break;
        }
    }

    /**
     * Called when the checked state of a compound button has changed.
     *
     * @param buttonView The compound button view whose state has changed.
     * @param isChecked  The new checked state of buttonView.
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.monitoring_button :
                if (isChecked) {
                    startMonitoring();
                    trainToggle.setEnabled(true);

                } else {
                    stopMonitoring();
                    trainToggle.setEnabled(false);
                }
                break;
            case R.id.train_button :
                long timestamp = System.currentTimeMillis();
                if (isChecked) {
                    CreateTrainingEventDialogFragment trainingFragment = CreateTrainingEventDialogFragment.newInstance();
                    trainingFragment.show(getFragmentManager(), "trainingdialog");
                } else {
                    if(startTimestamp == 0) return;
                    new TrainingTask(new Event(label, startTimestamp, timestamp, timestamp - startTimestamp, Event.EventTypes.valueOf(type), message), MainActivity.this).execute();
                    startTimestamp = 0;
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUIElements();
        setupSensors();
        setupListeners();
        startAwareServices();
        tcpListenerThread = new TCPListenerThread(this);
        tcpListenerThread.start();
    }

    @Override
    protected void onDestroy() {
        stopAwareServices();
        stopListenerThreads();
        super.onDestroy();
        tcpListenerThread.kill();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(statusReceiver, statusIntentFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(dtwReceiver, dtwIntentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(statusReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dtwReceiver);
    }

    /**
     * Method used to show a DeleteEventDialogFragment
     * Used to switch to DeleteEventDialogFragment from inside a ListEventsDialogFragment
     * @param item
     */
    public void showDeleteEventDialog(String item){
        DeleteEventDialogFragment newFragment = DeleteEventDialogFragment.newInstance(item);
        newFragment.show(getSupportFragmentManager(), "deleteeventdialog");
    }

    /**
     * Method to show a ListEventsDialogFragment
     * Used to switch to ListEventsDialogFragment from inside a DeleteEventDialogFragment
     */
    public void showListEventDialog() {
        ListEventsDialogFragment newFragment = ListEventsDialogFragment.newInstance();
        newFragment.show(getSupportFragmentManager(), "listeventsdialog");
    }

    /**
     * Sets the data necessary to create a new TrainingEvent
     * Used by CreateTrainingEventDialogFragment to return data
     * @param timestamp event start timestamp
     * @param label event label
     * @param message event message
     * @param type event type
     */
    public void setTrainingData(long timestamp, String label, String message, String type) {
        this.startTimestamp = timestamp;
        this.label = label;
        this.message = message;
        this.type = type;
    }

    /**
     * Creates UI buttons
     */
    private void setupUIElements() {
        Button setup = (Button) findViewById(R.id.setup_button);
        setup.setOnClickListener(this);
        Button exportDatabase = (Button) findViewById(R.id.check_database_button);
        exportDatabase.setOnClickListener(this);
        Button calibrateSensor = (Button) findViewById(R.id.calibrate_sensor_button);
        calibrateSensor.setOnClickListener(this);
        Button sendEvent = (Button) findViewById(R.id.send_event_button);
        sendEvent.setOnClickListener(this);
        Button dtw = (Button) findViewById(R.id.setup_dtw_button);
        dtw.setOnClickListener(this);
        Button deleteEvents = (Button) findViewById(R.id.list_events_button);
        deleteEvents.setOnClickListener(this);

        trainToggle = (ToggleButton) findViewById(R.id.train_button);
        trainToggle.setEnabled(false);
        trainToggle.setOnCheckedChangeListener(this);

        monitoringToggle = (ToggleButton) findViewById(R.id.monitoring_button);
        monitoringToggle.setOnCheckedChangeListener(this);

        //Listview to show broadcast messages in the UI
        ListView resultsView = (ListView) findViewById(R.id.monitoring_result);
        results = new ArrayList<>();
        resultsAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.list_results, results);
        resultsView.setAdapter(resultsAdapter);
        resultsView.setEmptyView(findViewById(R.id.emptyList));
    }

    /**
     * Set aware preferences for sensors using Config settings
     */
    private void setupSensors(){
        Aware.setSetting(this, Aware_Preferences.STATUS_LINEAR_ACCELEROMETER, AwareSettings.ACCELEROMETER_ENABLED);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LINEAR_ACCELEROMETER, AwareSettings.ACCELEROMETER_FREQUENCY);

        Aware.setSetting(this, Aware_Preferences.STATUS_LOCATION_GPS, AwareSettings.LOCATION_GPS_ENABLED);
        Aware.setSetting(this, Aware_Preferences.STATUS_LOCATION_NETWORK, AwareSettings.LOCATION_NETWORK_ENABLED);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LOCATION_GPS, AwareSettings.LOCATION_FREQUENCY);
        Aware.setSetting(this, Aware_Preferences.MIN_LOCATION_GPS_ACCURACY, AwareSettings.LOCATION_MIN_GPS_ACCURACY);
        Aware.setSetting(this, Aware_Preferences.LOCATION_EXPIRATION_TIME, AwareSettings.LOCATION_EXPIRATION_TIME);

        Aware.setSetting(this, com.aware.plugin.openweather.Settings.STATUS_PLUGIN_OPENWEATHER, AwareSettings.OPENWEATHER_ENABLED, com.aware.plugin.openweather.BuildConfig.APPLICATION_ID);
        Aware.setSetting(this, com.aware.plugin.openweather.Settings.PLUGIN_OPENWEATHER_FREQUENCY, AwareSettings.OPENWEATHER_FREQUENCY, com.aware.plugin.openweather.BuildConfig.APPLICATION_ID);
        Aware.setSetting(this, com.aware.plugin.openweather.Settings.OPENWEATHER_API_KEY, getResources().getString(R.string.openweather), com.aware.plugin.openweather.BuildConfig.APPLICATION_ID);
    }

    /**
     * Initialize listeners and listener thread
     */
    private void setupListeners() {
        sensorThread = new HandlerThread("SensorDataHandlerThread");
        sensorThread.start();
        Looper looper = sensorThread.getLooper();
        sensorHandler = new Handler(looper);

        dtwThread = new HandlerThread("DTWHandlerThread", Process.THREAD_PRIORITY_FOREGROUND);
        dtwThread.start();
        Looper dtwlooper = dtwThread.getLooper();
        dtwHandler = new Handler(dtwlooper);

        rotationReceiver = new RotationReceiver();
        linearAccelerometerReceiver = new LinearAccelerometerReceiver();
        locationReceiver = new LocationReceiver();
        temperatureReceiver = new TemperatureReceiver();
    }

    /**
     * Stop thread once message queue is empty
     */
    private void stopListenerThreads() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            sensorThread.quitSafely();
        } else {
            sensorThread.quit();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            dtwThread.quitSafely();
        } else {
            dtwThread.quit();
        }
    }

    /**
     * Start sensor data collecting listeners
     */
    private void startMonitoring(){
        rotationReceiver.register(this, sensorHandler);
        linearAccelerometerReceiver.register(this, sensorHandler);
        locationReceiver.register(this, sensorHandler);
        temperatureReceiver.register(this, sensorHandler);
    }

    /**
     * Stop sensor data collecting listeners
     */
    private void stopMonitoring(){
        rotationReceiver.unregister(this);
        linearAccelerometerReceiver.unregister(this);
        locationReceiver.unregister(this);
        temperatureReceiver.unregister(this);
        sensorHandler.removeCallbacksAndMessages(null);
        dtwHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Start Aware framework services required for the sensors we want to monitor
     */
    private void startAwareServices() {
        this.startService(new Intent(this, Aware.class));
        Aware.startLocations(this);
        Aware.startLinearAccelerometer(this);
        Aware.startPlugin(this, com.aware.plugin.openweather.BuildConfig.APPLICATION_ID);
    }

    /**
     * Stop Aware framework services required for the sensors we want to monitor
     */
    private void stopAwareServices() {
        Aware.setSetting(this, Aware_Preferences.STATUS_LOCATION_GPS, false);
        this.stopService(new Intent(this, Aware.class));
        stopMonitoring();
        Aware.stopAWARE();
        Aware.stopPlugin(this, com.aware.plugin.openweather.BuildConfig.APPLICATION_ID);
    }
}
