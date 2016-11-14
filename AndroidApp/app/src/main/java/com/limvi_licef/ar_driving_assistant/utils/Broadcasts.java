package com.limvi_licef.ar_driving_assistant.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public final class Broadcasts {

    public static final String ACTION_WRITE_TO_UI = "ACTION_WRITE_TO_UI";
    public static final String WRITE_MESSAGE = "WRITE_MESSAGE";

    private Broadcasts(){}

   /*
    * Send local broadcast with message to write to Main Activity monitoring textview
    */
    public static void sendWriteToUIBroadcast(Context context, String message) {
        Intent localIntent = new Intent(ACTION_WRITE_TO_UI).putExtra(WRITE_MESSAGE, message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }
}
