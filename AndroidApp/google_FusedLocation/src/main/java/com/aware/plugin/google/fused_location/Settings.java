
package com.aware.plugin.google.fused_location;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.aware.Aware;
import com.google.android.gms.location.LocationRequest;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Boolean to activate/deactivate Google Fused Location
     */
    public static final String STATUS_GOOGLE_FUSED_LOCATION = "status_google_fused_location";

    /**
     * How frequently should we try to acquire location (in seconds)
     */
    public static final String FREQUENCY_GOOGLE_FUSED_LOCATION = "frequency_google_fused_location";

    /**
     * How fast you are willing to get the latest location (in seconds)
     */
    public static final String MAX_FREQUENCY_GOOGLE_FUSED_LOCATION = "max_frequency_google_fused_location";

    /**
     * How important is accuracy to you and battery impact. One of the following:<br/>
     * {@link LocationRequest#PRIORITY_HIGH_ACCURACY}<br/>
     * {@link LocationRequest#PRIORITY_BALANCED_POWER_ACCURACY}<br/>
     * {@link LocationRequest#PRIORITY_LOW_POWER}<br/>
     * {@link LocationRequest#PRIORITY_NO_POWER}
     */
    public static final String ACCURACY_GOOGLE_FUSED_LOCATION = "accuracy_google_fused_location";

    /**
     * Wait these seconds before fallback from GPS fix
     */
    public static final String FALLBACK_LOCATION_TIMEOUT = "fallback_location_timeout";

    /**
     * Move X meters to trigger another location fix
     */
    public static final String LOCATION_SENSITIVITY = "location_sensitivity";

    private static CheckBoxPreference active;
    private static EditTextPreference update_frequency, max_update_frequency, fallback_timeout, location_sensitivity;
    private static ListPreference accuracy;

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

        active = (CheckBoxPreference) findPreference(STATUS_GOOGLE_FUSED_LOCATION);
        if (Aware.getSetting(this, STATUS_GOOGLE_FUSED_LOCATION).length() == 0)
            Aware.setSetting(this, STATUS_GOOGLE_FUSED_LOCATION, true);
        active.setChecked(Aware.getSetting(this, STATUS_GOOGLE_FUSED_LOCATION).equals("true"));

        update_frequency = (EditTextPreference) findPreference(FREQUENCY_GOOGLE_FUSED_LOCATION);
        if (Aware.getSetting(this, FREQUENCY_GOOGLE_FUSED_LOCATION).length() == 0)
            Aware.setSetting(this, FREQUENCY_GOOGLE_FUSED_LOCATION, 300);
        update_frequency.setSummary("Every " + Aware.getSetting(this, FREQUENCY_GOOGLE_FUSED_LOCATION) + " second(s)");

        max_update_frequency = (EditTextPreference) findPreference(MAX_FREQUENCY_GOOGLE_FUSED_LOCATION);
        if (Aware.getSetting(this, MAX_FREQUENCY_GOOGLE_FUSED_LOCATION).length() == 0)
            Aware.setSetting(this, MAX_FREQUENCY_GOOGLE_FUSED_LOCATION, 60);
        max_update_frequency.setSummary("Every " + Aware.getSetting(this, MAX_FREQUENCY_GOOGLE_FUSED_LOCATION) + " second(s)");

        fallback_timeout = (EditTextPreference) findPreference(FALLBACK_LOCATION_TIMEOUT);
        if (Aware.getSetting(this, FALLBACK_LOCATION_TIMEOUT).length() == 0)
            Aware.setSetting(this, FALLBACK_LOCATION_TIMEOUT, 20);
        fallback_timeout.setSummary("Wait " + Aware.getSetting(this, FALLBACK_LOCATION_TIMEOUT) + " second(s)");

        location_sensitivity = (EditTextPreference) findPreference(LOCATION_SENSITIVITY);
        if (Aware.getSetting(this, LOCATION_SENSITIVITY).length() == 0)
            Aware.setSetting(this, LOCATION_SENSITIVITY, 5);
        location_sensitivity.setSummary("More than " + Aware.getSetting(this, LOCATION_SENSITIVITY) + " meter(s)");

        accuracy = (ListPreference) findPreference(ACCURACY_GOOGLE_FUSED_LOCATION);
        if (Aware.getSetting(this, ACCURACY_GOOGLE_FUSED_LOCATION).length() == 0)
            Aware.setSetting(this, ACCURACY_GOOGLE_FUSED_LOCATION, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        accuracy.setSummary(getAccuracy(Integer.parseInt(Aware.getSetting(this, ACCURACY_GOOGLE_FUSED_LOCATION))));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (preference.getKey().equals(FREQUENCY_GOOGLE_FUSED_LOCATION)) {
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "300"));
            update_frequency.setSummary("Every " + Aware.getSetting(getApplicationContext(), FREQUENCY_GOOGLE_FUSED_LOCATION) + " second(s)");
        }
        if (preference.getKey().equals(MAX_FREQUENCY_GOOGLE_FUSED_LOCATION)) {
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "60"));
            max_update_frequency.setSummary("Every " + Aware.getSetting(getApplicationContext(), MAX_FREQUENCY_GOOGLE_FUSED_LOCATION) + " second(s)");
        }
        if (preference.getKey().equals(FALLBACK_LOCATION_TIMEOUT)) {
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "20"));
            fallback_timeout.setSummary("Wait " + Aware.getSetting(getApplicationContext(), FALLBACK_LOCATION_TIMEOUT) + " second(s)");
        }
        if (preference.getKey().equals(LOCATION_SENSITIVITY)) {
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "5"));
            location_sensitivity.setSummary("More than " + Aware.getSetting(getApplicationContext(), LOCATION_SENSITIVITY) + " meter(s)");
        }
        if (preference.getKey().equals(ACCURACY_GOOGLE_FUSED_LOCATION)) {
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, String.valueOf(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)));
            accuracy.setSummary(getAccuracy(Integer.parseInt(Aware.getSetting(this, ACCURACY_GOOGLE_FUSED_LOCATION))));
        }
        if (preference.getKey().equals(STATUS_GOOGLE_FUSED_LOCATION)) {
            Aware.setSetting(getApplicationContext(), key, sharedPreferences.getBoolean(key, false));
            active.setChecked(sharedPreferences.getBoolean(key, false));
        }
        if (Aware.getSetting(this, STATUS_GOOGLE_FUSED_LOCATION).equals("true")) {
            Aware.startPlugin(getApplicationContext(), "com.aware.plugin.google.fused_location");
        } else {
            Aware.stopPlugin(getApplicationContext(), "com.aware.plugin.google.fused_location");
        }
    }

    private String getAccuracy(int accuracy) {
        String[] readable = getResources().getStringArray(R.array.accuracies_readable);
        switch (accuracy) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
                return readable[0];
            case LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY:
                return readable[1];
            case LocationRequest.PRIORITY_LOW_POWER:
                return readable[2];
            case LocationRequest.PRIORITY_NO_POWER:
                return readable[3];
        }
        return readable[1];
    }
}
