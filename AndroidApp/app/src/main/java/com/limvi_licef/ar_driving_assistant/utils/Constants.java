package com.limvi_licef.ar_driving_assistant.utils;

public final class Constants {

    private Constants(){}

    public static final class FusedLocationPlugin {
        public static final String PACKAGE_NAME = "com.aware.plugin.google.fused_location";
        public static final String STATUS = "status_google_fused_location";
        public static final String FREQUENCY = "frequency_google_fused_location";
        public static final String MAX_FREQUENCY = "max_frequency_google_fused_location";
        public static final String ACCURACY = "accuracy_google_fused_location";
        public static final String FALLBACK_TIMEOUT = "fallback_location_timeout";
        public static final String LOCATION_SENSITIVITY = "location_sensitivity";
    }

    public static final class OpenWeatherPlugin {
        public static final String OPEN_WEATHER_PACKAGE = "com.aware.plugin.openweather";
        public static final String STATUS_OPEN_WEATHER = "status_plugin_openweather";
        public static final String FREQUENCY_OPEN_WEATHER = "plugin_openweather_frequency";
        public static final String API_KEY_OPEN_WEATHER = "api_key_plugin_openweather";
    }

}
