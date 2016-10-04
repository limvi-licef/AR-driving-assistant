package com.aware.plugin.openweather;

import android.Manifest;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ServiceCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.openweather.Provider.OpenWeather_Data;
import com.aware.ui.PermissionsHandler;
import com.aware.utils.Aware_Plugin;
import com.aware.utils.Http;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

public class Plugin extends Aware_Plugin implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /**
     * Shared context: new OpenWeather data is available
     */
    public static final String ACTION_AWARE_PLUGIN_OPENWEATHER = "ACTION_AWARE_PLUGIN_OPENWEATHER";

    /**
     * Extra string: openweather<br/>
     * JSONObject from OpenWeather<br/>
     */
    public static final String EXTRA_OPENWEATHER = "openweather";

    public static ContextProducer sContextProducer;
    public static ContentValues sOpenWeather;

    private static GoogleApiClient mGoogleApiClient;
    private final static LocationRequest locationRequest = new LocationRequest();
    private static PendingIntent pIntent;

    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE: OpenWeather";

        DATABASE_TABLES = Provider.DATABASE_TABLES;
        TABLES_FIELDS = Provider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{OpenWeather_Data.CONTENT_URI};

        CONTEXT_PRODUCER = new ContextProducer() {
            @Override
            public void onContext() {
                Intent mOpenWeather = new Intent(ACTION_AWARE_PLUGIN_OPENWEATHER);
                mOpenWeather.putExtra(EXTRA_OPENWEATHER, sOpenWeather);
                sendBroadcast(mOpenWeather);
            }
        };
        sContextProducer = CONTEXT_PRODUCER;

        //Permissions needed for our plugin
        REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        if (!is_google_services_available()) {
            if (DEBUG)
                Log.e(TAG, "Google Services Fused location are not available on this device");
        } else {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApiIfAvailable(LocationServices.API)
                    .build();

            Intent openWeatherIntent = new Intent(getApplicationContext(), OpenWeather_Service.class);
            pIntent = PendingIntent.getService(getApplicationContext(), 0, openWeatherIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Aware.startPlugin(this, "com.aware.plugin.openweather");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        boolean permissions_ok = true;
        for (String p : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                permissions_ok = false;
                break;
            }
        }

        if (permissions_ok) {
            DEBUG = Aware.getSetting(getApplicationContext(), Aware_Preferences.DEBUG_FLAG).equals("true");

            Aware.setSetting(this, Settings.STATUS_PLUGIN_OPENWEATHER, true);
            if (Aware.getSetting(getApplicationContext(), Settings.UNITS_PLUGIN_OPENWEATHER).length() == 0)
                Aware.setSetting(getApplicationContext(), Settings.UNITS_PLUGIN_OPENWEATHER, "metric");

            if (Aware.getSetting(getApplicationContext(), Settings.PLUGIN_OPENWEATHER_FREQUENCY).length() == 0)
                Aware.setSetting(getApplicationContext(), Settings.PLUGIN_OPENWEATHER_FREQUENCY, 60);

            if (Aware.getSetting(getApplicationContext(), Settings.OPENWEATHER_API_KEY).length() == 0)
                Aware.setSetting(getApplicationContext(), Settings.OPENWEATHER_API_KEY, "ada11fb870974565377df238f3046aa9");

            if (mGoogleApiClient != null && !mGoogleApiClient.isConnected())
                mGoogleApiClient.connect();

        } else {
            Intent permissions = new Intent(this, PermissionsHandler.class);
            permissions.putExtra(PermissionsHandler.EXTRA_REQUIRED_PERMISSIONS, REQUIRED_PERMISSIONS);
            permissions.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(permissions);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Aware.setSetting(this, Settings.STATUS_PLUGIN_OPENWEATHER, false);

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, pIntent);
            mGoogleApiClient.disconnect();
        }

        Aware.stopPlugin(this, "com.aware.plugin.openweather");
        //Aware.stopAWARE();
    }

    private boolean is_google_services_available() {
        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();
        int result = googleApi.isGooglePlayServicesAvailable(this);
        return (result == ConnectionResult.SUCCESS);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (DEBUG)
            Log.i(TAG, "Connected to Google Fused Location API");

        locationRequest.setInterval(Long.parseLong(Aware.getSetting(this, Settings.PLUGIN_OPENWEATHER_FREQUENCY)) * 60 * 1000);
        locationRequest.setFastestInterval(Long.parseLong(Aware.getSetting(this, Settings.PLUGIN_OPENWEATHER_FREQUENCY)) * 60 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, pIntent);
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (lastLocation != null) {
                Intent openWeatherIntent = new Intent(getApplicationContext(), OpenWeather_Service.class);
                openWeatherIntent.putExtra(LocationServices.FusedLocationApi.KEY_LOCATION_CHANGED, lastLocation);
                startService(openWeatherIntent);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (DEBUG)
            Log.w(TAG, "Error connecting to Google Fused Location services, will try again in 5 minutes");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (DEBUG)
            Log.w(TAG, "Error connecting to Google Fused Location services, will try again in 5 minutes");
    }
}
