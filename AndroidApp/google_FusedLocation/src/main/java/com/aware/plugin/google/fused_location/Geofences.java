package com.aware.plugin.google.fused_location;

import android.net.Uri;

import com.aware.utils.Aware_Plugin;

/**
 * Created by denzil on 08/06/16.
 */
public class Geofences extends Aware_Plugin {

    public static final String ACTION_AWARE_PLUGIN_FUSED_ENTERED_GEOFENCE = "ACTION_AWARE_PLUGIN_FUSED_ENTERED_GEOFENCE";
    public static final String ACTION_AWARE_PLUGIN_FUSED_EXITED_GEOFENCE = "ACTION_AWARE_PLUGIN_FUSED_EXITED_GEOFENCE";
    public static final String EXTRA_LABEL = "label";
    public static final String EXTRA_LOCATION = "location";
    public static final String EXTRA_RADIUS = "radius";

    public static final int STATUS_ENTER = 1;
    public static final int STATUS_EXIT = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        DATABASE_TABLES = Provider.DATABASE_TABLES;
        TABLES_FIELDS = Provider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{
                Provider.Geofences.CONTENT_URI,
                Provider.Geofences_Data.CONTENT_URI
        };
    }
}
