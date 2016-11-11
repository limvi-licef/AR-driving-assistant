package com.limvi_licef.ar_driving_assistant.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public final class Broadcasts {

    private Broadcasts(){}

   /*
    * Send local broadcast with message to write to Main Activity monitoring textview
    */
    public static void sendWriteToUIBroadcast(Context context, String message) {
        Intent localIntent = new Intent(Constants.ACTION_WRITE_TO_UI).putExtra(Constants.WRITE_MESSAGE, message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }
}
