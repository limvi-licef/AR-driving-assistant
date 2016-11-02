package com.limvi_licef.ar_driving_assistant.utils;

public abstract class Constants {

    public static final String USER_SHARED_PREFERENCES = "user_shared";
    public static final String ID_PREFERENCE = "user_id";
    public static final String IP_ADDRESS_PREFERENCE = "user_ip";

    //linear acceleration offset prefs
    public static final String OFFSET_X_PREF = "offset_x";
    public static final String OFFSET_Y_PREF = "offset_y";
    public static final String OFFSET_Z_PREF = "offset_z";

    //google fused location
    public static final String FUSED_LOCATION_PACKAGE = "com.aware.plugin.google.fused_location";
    public static final String STATUS_FUSED_LOCATION = "status_google_fused_location";
    public static final String FREQUENCY_FUSED_LOCATION = "frequency_google_fused_location";
    public static final String MAX_FREQUENCY_FUSED_LOCATION = "max_frequency_google_fused_location";
    public static final String ACCURACY_FUSED_LOCATION = "accuracy_google_fused_location";

    //openweather
    public static final String OPEN_WEATHER_PACKAGE = "com.aware.plugin.openweather";
    public static final String STATUS_OPEN_WEATHER = "status_plugin_openweather";
    public static final String FREQUENCY_OPEN_WEATHER = "plugin_openweather_frequency";
    public static final String API_KEY_OPEN_WEATHER = "api_key_plugin_openweather";

    public static final String ACTION_WRITE_TO_UI = "ACTION_WRITE_TO_UI";
    public static final String WRITE_MESSAGE = "WRITE_MESSAGE";

    //HoloLens communication
    public static final int HOLOLENS_PORT = 12345;
}
