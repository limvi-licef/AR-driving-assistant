package com.limvi_licef.ar_driving_assistant.activities;

import android.app.Activity;
import android.content.Intent;
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
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.R;

import java.util.ArrayList;

import com.limvi_licef.ar_driving_assistant.Settings;

public class MainActivity extends Activity {

    private ArrayList<String> results;
    private ArrayAdapter<String> resultsAdapter;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupAwareProviders();
        setupUIElements();
        db = DatabaseHelper.getHelper(this).getWritableDatabase();
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
                //TODO
            }
        });
        Button checkDatabase = (Button) findViewById(R.id.check_database_button);
        checkDatabase.setOnClickListener(new View.OnClickListener() {
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

        //fused location settings
        Aware.setSetting(this, Settings.fusedLocationStatus, true, Settings.fusedLocationPackageName);
        Aware.setSetting(this, Settings.fusedLocationFrequency, 10, Settings.fusedLocationPackageName);
        Aware.setSetting(this, Settings.fusedLocationMaxFrequency, 5, Settings.fusedLocationPackageName);
        Aware.setSetting(this, Settings.fusedLocationAccuracy, 102, Settings.fusedLocationPackageName);
        //TODO set all other sensors

    }

    private void startMonitoring() {
        Aware.startAWARE();
        Aware.startPlugin(this, Settings.fusedLocationPackageName);
    }

    private void stopMonitoring() {
        Aware.stopAWARE();
        Aware.stopPlugin(this, Settings.fusedLocationPackageName);
    }

}
