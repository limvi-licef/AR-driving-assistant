package com.limvi_licef.ar_driving_assistant.config;

/**
 * Contains constants used for communication with the Unity app
 */
public final class Communication {
    /**
     * Port that the HoloLens is listening on for receiving new events
     */
    public static final int HOLOLENS_PORT = 12345;

    public static final String JSON_REQUEST_TYPE_PARAM_EVENT = "event";
    public static final String JSON_EVENT_TYPE = "eventType";
    public static final String JSON_EVENT_MESSAGE = "message";

    public static final String JSON_REQUEST_TYPE = "requestType";
    public static final String JSON_REQUEST_TYPE_USERS = "GetUsersList";
    public static final String JSON_REQUEST_TYPE_PARAM_USER = "userList";
    public static final String JSON_USERS = "users";
    public static final String JSON_REQUEST_TYPE_INSERT_USER = "InsertNewUser";
    public static final String JSON_REQUEST_TYPE_PARAM_NEW_USER = "newUser";
    public static final String JSON_REQUEST_RETURN_VALUES_NAME = "userName";
    public static final String JSON_REQUEST_RETURN_VALUES_AGE = "userAge";
    public static final String JSON_REQUEST_RETURN_VALUES_GENDER = "userGender";
    public static final String JSON_REQUEST_RETURN_VALUES_AVATAR = "userAvatar";
    public static final String JSON_REQUEST_TYPE_LAST_KNOWN = "GetLastKnownRides";
    public static final String JSON_REQUEST_TYPE_PARAM_RIDES = "userRidesList";
    public static final String JSON_RIDES = "rides";
    public static final String JSON_REQUEST_ID = "userId";

    public static final String JSON_LAST_KNOWN_ERROR_MESSAGE = "Impossible de récupérer les derniers trajets";
    public static final String JSON_LAST_KNOWN_EMPTY_RESULT = "Aucunes données récupérées";
    public static final String JSON_LAST_KNOWN_SUCCESS = "Success";

    public static final String JSON_REQUEST_TYPE_PARAM_SPEED = "speedCounter";
    public final static String JSON_SPEED_UNITS = " km/h";

    public static final String JSON_RETURN_STATUS = "status";
}
