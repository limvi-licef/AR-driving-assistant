package com.limvi_licef.ar_driving_assistant.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.Gyroscope;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.R;

import java.util.ArrayList;

import com.limvi_licef.ar_driving_assistant.Settings;
import com.limvi_licef.ar_driving_assistant.fragments.UserDialogFragment;

public class MainActivity extends Activity {

    private ArrayList<String> results;
    private ArrayAdapter<String> resultsAdapter;
    private SQLiteDatabase db;

    //testing purposes
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ContentValues values = (ContentValues) intent.getExtras().get(Gyroscope.EXTRA_DATA);
            if(values == null || values.size() == 0) return;
            for(String key : values.keySet()) {
                results.add(key + " : " + values.getAsString(key));
            }
            resultsAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupAwareProviders();
        setupUIElements();
        setupListeners();
        db = DatabaseHelper.getHelper(this).getWritableDatabase();
    }

    @Override
    protected  void onDestroy() {
        unregisterReceiver(mReceiver);
    }

    private void setupUIElements() {
        //Setup buttons
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
                //TODO
            }
        });

        //Setup ListView for monitoring results
        ListView resultsView = (ListView) findViewById(R.id.monitoring_result);
        results = new ArrayList<>();
        resultsAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.list_results, results);
        resultsView.setAdapter(resultsAdapter);
        resultsView.setEmptyView(findViewById(R.id.emptyList));
    }

    private void setupAwareProviders(){
        Intent aware = new Intent(this, Aware.class);
        startService(aware);

        //gyroscope settings
        Aware.setSetting(this, Aware_Preferences.STATUS_GYROSCOPE, true);

        //accelerometer settings
        Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER, true);
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_ACCELEROMETER, 200000);

        //fused location plugin settings
        Aware.setSetting(this, Settings.STATUS_FUSED_LOCATION, true, Settings.FUSED_LOCATION_PACKAGE);
        Aware.setSetting(this, Settings.FREQUENCY_FUSED_LOCATION, 10, Settings.FUSED_LOCATION_PACKAGE);
        Aware.setSetting(this, Settings.MAX_FREQUENCY_FUSED_LOCATION, 5, Settings.FUSED_LOCATION_PACKAGE);
        Aware.setSetting(this, Settings.ACCURACY_FUSED_LOCATION, 102, Settings.FUSED_LOCATION_PACKAGE);
        //TODO set all other sensors

    }

    private void setupListeners(){
        IntentFilter broadcastFilter = new IntentFilter();
        broadcastFilter.addAction(Gyroscope.ACTION_AWARE_GYROSCOPE);
        registerReceiver(mReceiver, broadcastFilter);
    }

    private void startMonitoring() {
        Aware.startAWARE();
        Aware.startPlugin(this, Settings.FUSED_LOCATION_PACKAGE);
    }

    private void stopMonitoring() {
        Aware.stopAWARE();
        Aware.stopPlugin(this, Settings.FUSED_LOCATION_PACKAGE);
    }

}
