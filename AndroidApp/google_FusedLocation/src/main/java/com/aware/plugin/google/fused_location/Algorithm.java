/**
@author: denzil
*/
package com.aware.plugin.google.fused_location;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.providers.Locations_Provider.Locations_Data;
import com.google.android.gms.location.LocationServices;

public class Algorithm extends IntentService {

    public Algorithm() {
        super("Google Fused Location");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        
        boolean DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");
        
        if( intent != null && intent.hasExtra(LocationServices.FusedLocationApi.KEY_LOCATION_CHANGED ) ) {
        
            Location bestLocation = (Location) intent.getExtras().get(LocationServices.FusedLocationApi.KEY_LOCATION_CHANGED);

            if( bestLocation == null ) return;

            ContentValues rowData = new ContentValues();
            rowData.put(Locations_Data.TIMESTAMP, System.currentTimeMillis());
            rowData.put(Locations_Data.DEVICE_ID, Aware.getSetting(this,Aware_Preferences.DEVICE_ID));
            rowData.put(Locations_Data.LATITUDE, bestLocation.getLatitude());
            rowData.put(Locations_Data.LONGITUDE, bestLocation.getLongitude());
            rowData.put(Locations_Data.BEARING, bestLocation.getBearing());
            rowData.put(Locations_Data.SPEED, bestLocation.getSpeed());
            rowData.put(Locations_Data.ALTITUDE, bestLocation.getAltitude());
            rowData.put(Locations_Data.PROVIDER, bestLocation.getProvider());
            rowData.put(Locations_Data.ACCURACY, bestLocation.getAccuracy());
            
            getContentResolver().insert(Locations_Data.CONTENT_URI, rowData);
            
            if( DEBUG ) Log.d(Plugin.TAG, "Fused location:" + rowData.toString());

            Plugin.contextProducer.onContext();
        }
    }
}
