package com.aware.plugin.google.fused_location;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.support.annotation.Nullable;

import com.aware.Aware;
import com.aware.Aware_Preferences;

/**
 * Created by denzil on 08/06/16.
 */
public class GeofenceUtils {
    /**
     * Given current location, do we have a label for this location?
     * @param context
     * @param currentLocation
     * @return
     */
    public static String getLabel(Context context, Location currentLocation) {
        String label = "";
        Cursor geofences = getLabels(context, null);
        if (geofences != null && geofences.moveToFirst()) {
            do {
                Location labelLocation = new Location("Label location");
                labelLocation.setLatitude(geofences.getDouble(geofences.getColumnIndex(Provider.Geofences.GEO_LAT)));
                labelLocation.setLongitude(geofences.getDouble(geofences.getColumnIndex(Provider.Geofences.GEO_LONG)));
                if (GeofenceUtils.getDistance(currentLocation, labelLocation) <= 0.05) {
                    label = geofences.getString(geofences.getColumnIndex(Provider.Geofences.GEO_LABEL));
                }
            } while (geofences.moveToNext());
            geofences.close();
        }
        return ((label.length()>0) ? label : "");
    }

    /**
     * Given two coordinates, what's the distance between each other in kilometers
     * @param a
     * @param b
     * @return
     */
    public static float getDistance(Location a, Location b) {
        float[] distance = new float[2];
        Location.distanceBetween(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude(), distance);
        return distance[0]/1000;
    }

    /**
     * Given a label, return the location coordinates
     * @param context
     * @param label
     * @return
     */
    public static Location getLabelLocation(Context context, String label) {
        Location labelGPS = new Location("Fused location");
        Cursor labels = getLabels(context, label);
        if (labels != null && labels.moveToFirst()) {
            labelGPS.setLatitude(labels.getDouble(labels.getColumnIndex(Provider.Geofences.GEO_LAT)));
            labelGPS.setLongitude(labels.getDouble(labels.getColumnIndex(Provider.Geofences.GEO_LONG)));
        }
        if (labels != null && ! labels.isClosed()) labels.close();
        return labelGPS;
    }

    /**
     * Given a label, return the radius of the label's coordinates
     * @param context
     * @param label
     * @return
     */
    public static int getLabelLocationRadius(Context context, String label) {
        int radius = 1;
        Cursor labels = getLabels(context, label);
        if (labels != null && labels.moveToFirst()) {
            radius = labels.getInt(labels.getColumnIndex(Provider.Geofences.GEO_RADIUS));
        }
        if (labels != null && ! labels.isClosed()) labels.close();
        return radius;
    }

    /**
     * Save location label
     * @param context
     * @param label
     * @param currentLocation
     * @param radius
     */
    public static void saveLabel(Context context, String label, Location currentLocation, double radius) {
        if (! exists(context, label)) {
            ContentValues data = new ContentValues();
            data.put(Provider.Geofences.DEVICE_ID, Aware.getSetting(context, Aware_Preferences.DEVICE_ID));
            data.put(Provider.Geofences.TIMESTAMP, System.currentTimeMillis());
            data.put(Provider.Geofences.GEO_LABEL, label);
            data.put(Provider.Geofences.GEO_LAT, currentLocation.getLatitude());
            data.put(Provider.Geofences.GEO_LONG, currentLocation.getLongitude());
            data.put(Provider.Geofences.GEO_RADIUS, radius);
            context.getContentResolver().insert(Provider.Geofences.CONTENT_URI, data);
        } else {
            ContentValues data = new ContentValues();
            data.put(Provider.Geofences.TIMESTAMP, System.currentTimeMillis());
            data.put(Provider.Geofences.GEO_LAT, currentLocation.getLatitude());
            data.put(Provider.Geofences.GEO_LONG, currentLocation.getLongitude());
            data.put(Provider.Geofences.GEO_RADIUS, radius);
            context.getContentResolver().update(Provider.Geofences.CONTENT_URI, data, Provider.Geofences.GEO_LABEL + " LIKE '" + label + "'", null);
        }
    }

    /**
     * Get current defined geofence labels
     * @param context
     * @return
     */
    public static Cursor getLabels(Context context, @Nullable String filter_label) {
        if (filter_label != null && filter_label.length() > 0) {
            return context.getContentResolver().query(Provider.Geofences.CONTENT_URI, null, Provider.Geofences.GEO_LABEL + " LIKE '" + filter_label + "'", null, null);
        }
        return context.getContentResolver().query(Provider.Geofences.CONTENT_URI, null, null, null, null);
    }

    public static boolean exists(Context context, String name) {
        boolean exists = false;
        Cursor labels = context.getContentResolver().query(Provider.Geofences.CONTENT_URI, null,
                Provider.Geofences.GEO_LABEL + " LIKE '" + name + "'", null, null);
        if (labels != null && labels.getCount() > 0) exists = true;
        if (labels != null && ! labels.isClosed()) labels.close();
        return exists;
    }
}
