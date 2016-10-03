package com.aware.plugin.openweather;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Http;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by denzil on 02/08/16.
 */
public class OpenWeather_Service extends IntentService {

    public OpenWeather_Service() {
        super(Plugin.TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

        if (intent != null && intent.hasExtra(LocationServices.FusedLocationApi.KEY_LOCATION_CHANGED)) {

            Location location = (Location) intent.getExtras().get(LocationServices.FusedLocationApi.KEY_LOCATION_CHANGED);

            if (location == null) return;

            Http httpObj = new Http(this);
            String server_response = httpObj.dataGET(
                    String.format(Settings.OPENWEATHER_API_URL,
                            location.getLatitude(),
                            location.getLongitude(),
                            Locale.getDefault().getLanguage(),
                            Aware.getSetting(getApplicationContext(), Settings.UNITS_PLUGIN_OPENWEATHER),
                            Aware.getSetting(getApplicationContext(), Settings.OPENWEATHER_API_KEY)),
                    false);

            if (server_response == null || server_response.length() == 0 || server_response.contains("Invalid API key"))
                return;

            try {
                JSONObject raw_data = new JSONObject(server_response);

                if (Plugin.DEBUG) Log.d(Plugin.TAG, "OpenWeather answer: " + raw_data.toString(5));

                JSONObject wind = raw_data.getJSONObject("wind");
                JSONObject weather_characteristics = raw_data.getJSONObject("main");
                JSONObject weather = raw_data.getJSONArray("weather").getJSONObject(0);
                JSONObject clouds = raw_data.getJSONObject("clouds");

                JSONObject rain = null;
                if (raw_data.opt("rain") != null) {
                    rain = raw_data.optJSONObject("rain");
                }
                JSONObject snow = null;
                if (raw_data.opt("snow") != null) {
                    snow = raw_data.optJSONObject("snow");
                }
                JSONObject sys = raw_data.getJSONObject("sys");

                ContentValues weather_data = new ContentValues();
                weather_data.put(Provider.OpenWeather_Data.TIMESTAMP, System.currentTimeMillis());
                weather_data.put(Provider.OpenWeather_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
                weather_data.put(Provider.OpenWeather_Data.CITY, raw_data.getString("name"));
                weather_data.put(Provider.OpenWeather_Data.TEMPERATURE, weather_characteristics.getDouble("temp"));
                weather_data.put(Provider.OpenWeather_Data.TEMPERATURE_MAX, weather_characteristics.getDouble("temp_max"));
                weather_data.put(Provider.OpenWeather_Data.TEMPERATURE_MIN, weather_characteristics.getDouble("temp_min"));
                weather_data.put(Provider.OpenWeather_Data.UNITS, Aware.getSetting(getApplicationContext(), Settings.UNITS_PLUGIN_OPENWEATHER));
                weather_data.put(Provider.OpenWeather_Data.HUMIDITY, weather_characteristics.getDouble("humidity"));
                weather_data.put(Provider.OpenWeather_Data.PRESSURE, weather_characteristics.getDouble("pressure"));
                weather_data.put(Provider.OpenWeather_Data.WIND_SPEED, wind.getDouble("speed"));
                weather_data.put(Provider.OpenWeather_Data.WIND_DEGREES, wind.getDouble("deg"));
                weather_data.put(Provider.OpenWeather_Data.CLOUDINESS, clouds.getDouble("all"));

                double rain_value = 0;
                if (rain != null) {
                    if (rain.opt("1h") != null) {
                        rain_value = rain.optDouble("1h", 0);
                    } else if (rain.opt("3h") != null) {
                        rain_value = rain.optDouble("3h", 0);
                    } else if (rain.opt("6h") != null) {
                        rain_value = rain.optDouble("6h", 0);
                    } else if (rain.opt("12h") != null) {
                        rain_value = rain.optDouble("12h", 0);
                    } else if (rain.opt("24h") != null) {
                        rain_value = rain.optDouble("24h", 0);
                    } else if (rain.opt("day") != null) {
                        rain_value = rain.optDouble("day", 0);
                    }
                }

                double snow_value = 0;
                if (snow != null) {
                    if (snow.opt("1h") != null) {
                        snow_value = snow.optDouble("1h", 0);
                    } else if (snow.opt("3h") != null) {
                        snow_value = snow.optDouble("3h", 0);
                    } else if (snow.opt("6h") != null) {
                        snow_value = snow.optDouble("6h", 0);
                    } else if (snow.opt("12h") != null) {
                        snow_value = snow.optDouble("12h", 0);
                    } else if (snow.opt("24h") != null) {
                        snow_value = snow.optDouble("24h", 0);
                    } else if (snow.opt("day") != null) {
                        snow_value = snow.optDouble("day", 0);
                    }
                }
                weather_data.put(Provider.OpenWeather_Data.RAIN, rain_value);
                weather_data.put(Provider.OpenWeather_Data.SNOW, snow_value);
                weather_data.put(Provider.OpenWeather_Data.SUNRISE, sys.getDouble("sunrise"));
                weather_data.put(Provider.OpenWeather_Data.SUNSET, sys.getDouble("sunset"));
                weather_data.put(Provider.OpenWeather_Data.WEATHER_ICON_ID, weather.getInt("id"));
                weather_data.put(Provider.OpenWeather_Data.WEATHER_DESCRIPTION, weather.getString("main") + ": " + weather.getString("description"));

                getContentResolver().insert(Provider.OpenWeather_Data.CONTENT_URI, weather_data);

                Plugin.sOpenWeather = weather_data;
                Plugin.sContextProducer.onContext();

                if (Plugin.DEBUG) Log.d(Plugin.TAG, weather_data.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}
