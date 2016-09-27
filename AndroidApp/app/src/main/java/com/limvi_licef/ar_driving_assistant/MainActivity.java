package com.limvi_licef.ar_driving_assistant;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MainActivity extends Activity {

    //Objects to monitor
    private static final LinkedHashMap<String, String> monitoringTargets;
    static
    {
        monitoringTargets = new LinkedHashMap<String, String>();
        monitoringTargets.put("Speed", "");
    }

    private ArrayList<MonitoringResult> results;
    private ListAdapter resultsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup buttons
        ToggleButton monitoringToggle = (ToggleButton) findViewById(R.id.monitoring_button);
        monitoringToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean toggled) {
                if (toggled) {
                    //TODO

                } else {
                    //TODO
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
        setupResultList();
        resultsAdapter = new ArrayAdapter<MonitoringResult>(MainActivity.this, R.layout.list_results, results);
        resultsView.setAdapter(resultsAdapter);
        resultsView.setEmptyView(findViewById(R.id.emptyList));

    }

    private void setupResultList(){
        for(String key : monitoringTargets.keySet()){
            results.add(new MonitoringResult(key, ""));
        }
    }

    private class MonitoringResult {
        private String key;
        private String value;

        public MonitoringResult(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return this.key + " : " + this.value;
        }
    }
}
