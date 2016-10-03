package com.aware.plugin.openweather;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.aware.Aware;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    /**
     * OpenWeather API endpoint
     */
    public static final String OPENWEATHER_API_URL = "http://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&lang=%s&units=%s&appid=%s";

    /**
     * State
     */
    public static final String STATUS_PLUGIN_OPENWEATHER = "status_plugin_openweather";

    /**
     * Measurement units
     */
    public static final String UNITS_PLUGIN_OPENWEATHER = "units_plugin_openweather";

    /**
     * How frequently we status the weather conditions
     */
    public static final String PLUGIN_OPENWEATHER_FREQUENCY = "plugin_openweather_frequency";

    /**
     * Openweather API key
     */
    public static final String OPENWEATHER_API_KEY = "api_key_plugin_openweather";

    private static CheckBoxPreference status;
    private static ListPreference units;
    private static EditTextPreference frequency, openweather_api_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        status = (CheckBoxPreference) findPreference(STATUS_PLUGIN_OPENWEATHER);
        if (Aware.getSetting(this, STATUS_PLUGIN_OPENWEATHER).length() == 0)
            Aware.setSetting(this, STATUS_PLUGIN_OPENWEATHER, true);
        status.setChecked(Aware.getSetting(this, STATUS_PLUGIN_OPENWEATHER).equals("true"));

        units = (ListPreference) findPreference(UNITS_PLUGIN_OPENWEATHER);
        if (Aware.getSetting(this, UNITS_PLUGIN_OPENWEATHER).length() == 0)
            Aware.setSetting(this, UNITS_PLUGIN_OPENWEATHER, "metric");
        units.setSummary(Aware.getSetting(this, UNITS_PLUGIN_OPENWEATHER));

        frequency = (EditTextPreference) findPreference(PLUGIN_OPENWEATHER_FREQUENCY);
        if (Aware.getSetting(this, PLUGIN_OPENWEATHER_FREQUENCY).length() == 0)
            Aware.setSetting(this, PLUGIN_OPENWEATHER_FREQUENCY, 60);
        frequency.setText(Aware.getSetting(this, PLUGIN_OPENWEATHER_FREQUENCY));
        frequency.setSummary("Every " + Aware.getSetting(this, PLUGIN_OPENWEATHER_FREQUENCY) + " minute(s)");

        openweather_api_key = (EditTextPreference) findPreference(OPENWEATHER_API_KEY);
        if (Aware.getSetting(this, OPENWEATHER_API_KEY).length() == 0)
            Aware.setSetting(this, OPENWEATHER_API_KEY, "ada11fb870974565377df238f3046aa9");
        openweather_api_key.setText(Aware.getSetting(this, OPENWEATHER_API_KEY));
        openweather_api_key.setSummary(Aware.getSetting(this, OPENWEATHER_API_KEY));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (preference.getKey().equals(STATUS_PLUGIN_OPENWEATHER)) {
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getBoolean(key, false));
            status.setChecked(sharedPreferences.getBoolean(key, false));
        }
        if (preference.getKey().equals(UNITS_PLUGIN_OPENWEATHER)) {
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "metric"));
            preference.setSummary(Aware.getSetting(this, UNITS_PLUGIN_OPENWEATHER));
        }
        if (preference.getKey().equals(PLUGIN_OPENWEATHER_FREQUENCY)) {
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "60"));
            preference.setSummary("Every " + Aware.getSetting(this, PLUGIN_OPENWEATHER_FREQUENCY) + " minute(s)");
        }
        if (preference.getKey().equals(OPENWEATHER_API_KEY)) {
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "ada11fb870974565377df238f3046aa9"));
        }

        if (Aware.getSetting(this, STATUS_PLUGIN_OPENWEATHER).equals("true")) {
            Aware.startPlugin(getApplicationContext(), "com.aware.plugin.openweather");
        } else {
            Aware.stopPlugin(getApplicationContext(), "com.aware.plugin.openweather");
        }
    }
}
