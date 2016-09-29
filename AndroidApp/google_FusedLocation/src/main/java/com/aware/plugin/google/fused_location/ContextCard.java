package com.aware.plugin.google.fused_location;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.aware.Aware;
import com.aware.providers.Locations_Provider;
import com.aware.utils.IContextCard;

import java.io.IOException;
import java.util.List;

public class ContextCard implements IContextCard {

    public ContextCard() {
    }

    private ListView geofences;
    private GeofencesAdapter adapter;

    @Override
    public View getContextCard(final Context context) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View card = inflater.inflate(R.layout.card, null);
        TextView address = (TextView) card.findViewById(R.id.address);
        TextView last_update = (TextView) card.findViewById(R.id.last_updated);
        Button geofencer = (Button) card.findViewById(R.id.geofencer);

        geofences = (ListView) card.findViewById(R.id.geofences_list);

        geofences.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true); //allow scrolling of this view
                return false;
            }
        });

        ViewGroup.LayoutParams params = geofences.getLayoutParams();
        params.height = 400;
        geofences.setLayoutParams(params);

        adapter = new GeofencesAdapter(context, context.getContentResolver().query(Provider.Geofences.CONTENT_URI, null, null, null, Provider.Geofences.GEO_LABEL + " ASC"), true);
        geofences.setAdapter(adapter);

        geofencer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Location user_location = new Location("Current Location");

                Uri locationURI = Uri.parse("content://" + context.getPackageName() + ".provider.locations/locations");
                Cursor last_location = context.getContentResolver().query(locationURI, null, null, null, Locations_Provider.Locations_Data.TIMESTAMP + " DESC LIMIT 1");
                if (last_location != null && last_location.moveToFirst()) {
                    user_location.setLatitude(last_location.getDouble(last_location.getColumnIndex(Locations_Provider.Locations_Data.LATITUDE)));
                    user_location.setLongitude(last_location.getDouble(last_location.getColumnIndex(Locations_Provider.Locations_Data.LONGITUDE)));
                    user_location.setAccuracy(last_location.getFloat(last_location.getColumnIndex(Locations_Provider.Locations_Data.ACCURACY)));
                }
                if (last_location != null && !last_location.isClosed()) last_location.close();

                Intent locationGeofencer = new Intent(context, GeofenceMap.class);
                locationGeofencer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (GeofenceUtils.getLabel(context, user_location).length()>0) {
                    locationGeofencer.putExtra(GeofenceMap.EXTRA_LABEL, GeofenceUtils.getLabel(context, user_location));
                }
                context.startActivity(locationGeofencer);
            }
        });

        Uri locationURI = Uri.parse("content://" + context.getPackageName() + ".provider.locations/locations");
        Cursor last_location = context.getContentResolver().query(locationURI, null, null, null, Locations_Provider.Locations_Data.TIMESTAMP + " DESC LIMIT 1");
        if (last_location != null && last_location.moveToFirst()) {
            double lat = last_location.getDouble(last_location.getColumnIndex(Locations_Provider.Locations_Data.LATITUDE));
            double lon = last_location.getDouble(last_location.getColumnIndex(Locations_Provider.Locations_Data.LONGITUDE));
            long timestamp = last_location.getLong(last_location.getColumnIndex(Locations_Provider.Locations_Data.TIMESTAMP));

            Location user_location = new Location("Current Location");
            user_location.setLatitude(lat);
            user_location.setLongitude(lon);
            user_location.setAccuracy(last_location.getFloat(last_location.getColumnIndex(Locations_Provider.Locations_Data.ACCURACY)));

            last_update.setText(String.format("%s", DateUtils.getRelativeTimeSpanString(timestamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()));

            try {
                Geocoder geo = new Geocoder(context);
                String geo_text = "";
                List<Address> addressList = geo.getFromLocation(lat, lon, 1);
                for (int i = 0; i < addressList.size(); i++) {
                    Address address1 = addressList.get(i);
                    for (int j = 0; j < address1.getMaxAddressLineIndex(); j++) {
                        if (address1.getAddressLine(j).length() > 0) {
                            geo_text += address1.getAddressLine(j) + "\n";
                        }
                    }
                    geo_text += address1.getCountryName();
                }

                geo_text += "\nGeofence: " + GeofenceUtils.getLabel(context, user_location) + " (" + last_location.getFloat(last_location.getColumnIndex(Locations_Provider.Locations_Data.ACCURACY)) + " meters)";

                address.setText(geo_text);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (last_location != null && !last_location.isClosed()) last_location.close();

        return card;
    }

    public class GeofencesAdapter extends CursorAdapter {

        private Context mContext;

        public GeofencesAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            mContext = context;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.geofences_row, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final TextView label = (TextView) view.findViewById(R.id.geofence_label);
            final Button edit = (Button) view.findViewById(R.id.geofence_edit);
            final Button delete = (Button) view.findViewById(R.id.geofence_del);

            final String labeltxt = cursor.getString(cursor.getColumnIndex(Provider.Geofences.GEO_LABEL));

            final Location currentCoords = new Location("Current location");
            final Location labelCoords = new Location("Label location");

            Uri locationURI = Uri.parse("content://" + context.getPackageName() + ".provider.locations/locations");
            Cursor currentLocation = context.getContentResolver().query(locationURI, null, null, null, Locations_Provider.Locations_Data.TIMESTAMP + " DESC LIMIT 1");
            if (currentLocation != null && currentLocation.moveToFirst()) {
                currentCoords.setLatitude(currentLocation.getDouble(currentLocation.getColumnIndex(Locations_Provider.Locations_Data.LATITUDE)));
                currentCoords.setLongitude(currentLocation.getDouble(currentLocation.getColumnIndex(Locations_Provider.Locations_Data.LONGITUDE)));

                currentLocation.close();
            }

            labelCoords.setLatitude(cursor.getDouble(cursor.getColumnIndex(Locations_Provider.Locations_Data.LATITUDE)));
            labelCoords.setLongitude(cursor.getDouble(cursor.getColumnIndex(Locations_Provider.Locations_Data.LONGITUDE)));

            label.setText(labeltxt + "\n" + GeofenceUtils.getDistance(currentCoords, labelCoords) + " km away");

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent locationGeofencer = new Intent(mContext, GeofenceMap.class);
                    locationGeofencer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    locationGeofencer.putExtra(GeofenceMap.EXTRA_LABEL, labeltxt);
                    mContext.startActivity(locationGeofencer);
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.getContentResolver().delete(Provider.Geofences.CONTENT_URI, Provider.Geofences.GEO_LABEL + " LIKE '" + labeltxt + "'", null);
                    adapter.changeCursor(mContext.getContentResolver().query(Provider.Geofences.CONTENT_URI, null, null, null, Provider.Geofences.GEO_LABEL + " ASC"));
                }
            });
        }
    }
}
