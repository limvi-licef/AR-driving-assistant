package com.limvi_licef.ar_driving_assistant.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public final class Broadcasts {

    public static final String ACTION_START_DTW = "ACTION_START_DTW";
    public static final String ACTION_WRITE_TO_UI = "ACTION_WRITE_TO_UI";
    public static final String WRITE_MESSAGE = "WRITE_MESSAGE";

    private Broadcasts(){}

    /**
     * Send local broadcast with message to write to Main Activity monitoring textview
     * @param context
     * @param message the message to write to the UI
     */
    public static void sendWriteToUIBroadcast(Context context, String message) {
        Intent localIntent = new Intent(ACTION_WRITE_TO_UI).putExtra(WRITE_MESSAGE, message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }

    /**
     * Send local broadcast to indicate that the DTW algorithm launched
     * @param context
     */
    public static void sendStartDTWBroadcast(Context context) {
        Intent localIntent = new Intent(ACTION_START_DTW);
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }
}
