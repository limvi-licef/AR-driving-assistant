
package com.aware.plugin.google.fused_location;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.providers.Locations_Provider;
import com.aware.providers.Locations_Provider.Locations_Data;
import com.aware.ui.PermissionsHandler;
import com.aware.utils.Aware_Plugin;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Fused location service for Aware framework
 * Requires Google Services API available on the device.
 *
 * @author denzil
 */
public class Plugin extends Aware_Plugin implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /**
     * Broadcasted event: new location available
     */
    public static final String ACTION_AWARE_LOCATIONS = "ACTION_AWARE_LOCATIONS";
    public static final String EXTRA_DATA = "data";

    /**
     * This plugin's package name
     */
    private final String PACKAGE_NAME = "com.aware.plugin.google.fused_location";

    private static GoogleApiClient mLocationClient;
    private final static LocationRequest mLocationRequest = new LocationRequest();
    private static PendingIntent pIntent;

    public static ContextProducer contextProducer;

    private static Location lastGeofence;

    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::Google Fused Location";

        DATABASE_TABLES = Locations_Provider.DATABASE_TABLES;
        TABLES_FIELDS = Locations_Provider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{Locations_Data.CONTENT_URI};

        CONTEXT_PRODUCER = new ContextProducer() {
            @Override
            public void onContext() {
                Location currentLocation = new Location("Current location");
                Cursor data = getContentResolver().query(Locations_Data.CONTENT_URI, null, null, null, Locations_Data.TIMESTAMP + " DESC LIMIT 1");
                if (data != null && data.moveToFirst()) {
                    currentLocation.setLatitude(data.getDouble(data.getColumnIndex(Locations_Data.LATITUDE)));
                    currentLocation.setLongitude(data.getDouble(data.getColumnIndex(Locations_Data.LONGITUDE)));
                    currentLocation.setAccuracy(data.getFloat(data.getColumnIndex(Locations_Data.ACCURACY)));
                }
                if (data != null && !data.isClosed()) data.close();

                Intent context = new Intent(ACTION_AWARE_LOCATIONS);
                context.putExtra(Plugin.EXTRA_DATA, currentLocation);
                sendBroadcast(context);

                checkGeofences();
            }
        };
        contextProducer = CONTEXT_PRODUCER;

        REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_FINE_LOCATION);

        if (!is_google_services_available()) {
            if (DEBUG)
                Log.e(TAG, "Google Services fused location is not available on this device.");
        } else {
            mLocationClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApiIfAvailable(LocationServices.API)
                    .build();

            Intent locationIntent = new Intent(this, com.aware.plugin.google.fused_location.Algorithm.class);
            pIntent = PendingIntent.getService(this, 0, locationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent geofences = new Intent(this, com.aware.plugin.google.fused_location.Geofences.class);
            startService(geofences);

            Aware.startPlugin(this, PACKAGE_NAME);
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

            DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

            Aware.setSetting(this, Settings.STATUS_GOOGLE_FUSED_LOCATION, true);
            if (Aware.getSetting(this, Settings.FREQUENCY_GOOGLE_FUSED_LOCATION).length() == 0)
                Aware.setSetting(this, Settings.FREQUENCY_GOOGLE_FUSED_LOCATION, 300);

            if (Aware.getSetting(this, Settings.MAX_FREQUENCY_GOOGLE_FUSED_LOCATION).length() == 0)
                Aware.setSetting(this, Settings.MAX_FREQUENCY_GOOGLE_FUSED_LOCATION, 60);

            if (Aware.getSetting(this, Settings.ACCURACY_GOOGLE_FUSED_LOCATION).length() == 0)
                Aware.setSetting(this, Settings.ACCURACY_GOOGLE_FUSED_LOCATION, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            if (Aware.getSetting(this, Settings.FALLBACK_LOCATION_TIMEOUT).length() == 0)
                Aware.setSetting(this, Settings.FALLBACK_LOCATION_TIMEOUT, 20);

            if (Aware.getSetting(this, Settings.LOCATION_SENSITIVITY).length() == 0)
                Aware.setSetting(this, Settings.LOCATION_SENSITIVITY, 5);

            if (mLocationClient != null && !mLocationClient.isConnected())
                mLocationClient.connect();

            checkGeofences(); //checks the geofences every 5 minutes

        } else {
            Intent permissions = new Intent(this, PermissionsHandler.class);
            permissions.putExtra(PermissionsHandler.EXTRA_REQUIRED_PERMISSIONS, REQUIRED_PERMISSIONS);
            permissions.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(permissions);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * How are we doing regarding the geofences?
     */
    private void checkGeofences() {
        Location currentLocation = new Location("Current location");
        Cursor data = getContentResolver().query(Locations_Data.CONTENT_URI, null, null, null, Locations_Data.TIMESTAMP + " DESC LIMIT 1");
        if (data != null && data.moveToFirst()) {
            currentLocation.setLatitude(data.getDouble(data.getColumnIndex(Locations_Data.LATITUDE)));
            currentLocation.setLongitude(data.getDouble(data.getColumnIndex(Locations_Data.LONGITUDE)));
            currentLocation.setAccuracy(data.getFloat(data.getColumnIndex(Locations_Data.ACCURACY)));
            currentLocation.setTime(data.getLong(data.getColumnIndex(Locations_Data.TIMESTAMP)));
        }
        if (data != null && !data.isClosed()) data.close();

        Cursor geofences = GeofenceUtils.getLabels(this, null); //get list of defined geofences
        if (geofences != null && geofences.moveToFirst()) {
            do {
                Location geofenceLocation = new Location("Geofence location");
                geofenceLocation.setLatitude(geofences.getDouble(geofences.getColumnIndex(Provider.Geofences.GEO_LAT)));
                geofenceLocation.setLongitude(geofences.getDouble(geofences.getColumnIndex(Provider.Geofences.GEO_LONG)));

                //Current location is within this geofence
                if (GeofenceUtils.getDistance(currentLocation, geofenceLocation) <= 0.05) { //50 meters (0.05 km)
                    //First time in this geofence
                    if (lastGeofence == null) {
                        Intent geofenced = new Intent(Geofences.ACTION_AWARE_PLUGIN_FUSED_ENTERED_GEOFENCE);
                        geofenced.putExtra(Geofences.EXTRA_LABEL, geofences.getString(geofences.getColumnIndex(Provider.Geofences.GEO_LABEL)));
                        geofenced.putExtra(Geofences.EXTRA_LOCATION, geofenceLocation);
                        geofenced.putExtra(Geofences.EXTRA_RADIUS, geofences.getDouble(geofences.getColumnIndex(Provider.Geofences.GEO_RADIUS)));
                        sendBroadcast(geofenced);

                        ContentValues entered = new ContentValues();
                        entered.put(Provider.Geofences_Data.TIMESTAMP, System.currentTimeMillis());
                        entered.put(Provider.Geofences_Data.DEVICE_ID, Aware.getSetting(this, Aware_Preferences.DEVICE_ID));
                        entered.put(Provider.Geofences_Data.GEO_LABEL, geofences.getString(geofences.getColumnIndex(Provider.Geofences.GEO_LABEL)));
                        entered.put(Provider.Geofences_Data.GEO_LAT, geofences.getDouble(geofences.getColumnIndex(Provider.Geofences.GEO_LAT)));
                        entered.put(Provider.Geofences_Data.GEO_LONG, geofences.getString(geofences.getColumnIndex(Provider.Geofences.GEO_LONG)));
                        entered.put(Provider.Geofences_Data.DISTANCE, GeofenceUtils.getDistance(currentLocation, geofenceLocation));
                        entered.put(Provider.Geofences_Data.STATUS, Geofences.STATUS_ENTER);

                        if (Aware.DEBUG)
                            Log.d(Aware.TAG, "Geofence enter: \n"+ entered.toString());

                        getContentResolver().insert(Provider.Geofences_Data.CONTENT_URI, entered);

                        lastGeofence = geofenceLocation;
                        break;
                    }
                }
            } while (geofences.moveToNext());
            geofences.close();

            if (lastGeofence != null && GeofenceUtils.getDistance(currentLocation, lastGeofence) > 0.05) { //exited last geofence

                String label = GeofenceUtils.getLabel(this, lastGeofence);
                long radius = GeofenceUtils.getLabelLocationRadius(this, GeofenceUtils.getLabel(this, lastGeofence));

                Intent geofenced = new Intent(Geofences.ACTION_AWARE_PLUGIN_FUSED_EXITED_GEOFENCE);
                geofenced.putExtra(Geofences.EXTRA_LABEL, label);
                geofenced.putExtra(Geofences.EXTRA_LOCATION, lastGeofence);
                geofenced.putExtra(Geofences.EXTRA_RADIUS, radius);
                sendBroadcast(geofenced);

                ContentValues exited = new ContentValues();
                exited.put(Provider.Geofences_Data.TIMESTAMP, System.currentTimeMillis());
                exited.put(Provider.Geofences_Data.DEVICE_ID, Aware.getSetting(this, Aware_Preferences.DEVICE_ID));
                exited.put(Provider.Geofences_Data.GEO_LABEL, label);
                exited.put(Provider.Geofences_Data.GEO_LAT, lastGeofence.getLatitude());
                exited.put(Provider.Geofences_Data.GEO_LONG, lastGeofence.getLongitude());
                exited.put(Provider.Geofences_Data.DISTANCE, GeofenceUtils.getDistance(currentLocation, lastGeofence));
                exited.put(Provider.Geofences_Data.STATUS, Geofences.STATUS_EXIT);

                getContentResolver().insert(Provider.Geofences_Data.CONTENT_URI, exited);

                if (Aware.DEBUG)
                    Log.d(Aware.TAG, "Geofence exit:\n"+ exited.toString());

                lastGeofence = null;
            }
        } else {
            lastGeofence = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Aware.setSetting(this, Settings.STATUS_GOOGLE_FUSED_LOCATION, false);

        if (mLocationClient != null && mLocationClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, pIntent);
            mLocationClient.disconnect();
        }

        Intent geofences = new Intent(this, com.aware.plugin.google.fused_location.Geofences.class);
        stopService(geofences);

        Aware.stopPlugin(this, PACKAGE_NAME);
        Aware.stopAWARE();
    }

    private boolean is_google_services_available() {
        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();
        int result = googleApi.isGooglePlayServicesAvailable(this);
        return (result == ConnectionResult.SUCCESS);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connection_result) {
        if (DEBUG)
            Log.w(TAG, "Error connecting to Google Fused Location services, will try again in 5 minutes");
    }

    @Override
    public void onConnected(Bundle arg0) {
        if (DEBUG)
            Log.i(TAG, "Connected to Google's Location API");

        mLocationRequest.setPriority(Integer.parseInt(Aware.getSetting(this, Settings.ACCURACY_GOOGLE_FUSED_LOCATION)));
        mLocationRequest.setInterval(Long.parseLong(Aware.getSetting(this, Settings.FREQUENCY_GOOGLE_FUSED_LOCATION)) * 1000);
        mLocationRequest.setFastestInterval(Long.parseLong(Aware.getSetting(this, Settings.MAX_FREQUENCY_GOOGLE_FUSED_LOCATION)) * 1000);
        mLocationRequest.setMaxWaitTime(Long.parseLong(Aware.getSetting(this, Settings.FALLBACK_LOCATION_TIMEOUT)) * 1000); //wait X seconds for GPS
        mLocationRequest.setSmallestDisplacement(Float.parseFloat(Aware.getSetting(this, Settings.LOCATION_SENSITIVITY)));

        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, pIntent);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (DEBUG)
            Log.w(TAG, "Error connecting to Google Fused Location services, will try again in 5 minutes");
    }
}
