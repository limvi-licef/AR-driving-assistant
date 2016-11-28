package com.limvi_licef.ar_driving_assistant.receivers;

import android.content.Context;
import android.os.Handler;

public interface SensorReceiver {

    /**
     * Register the receiver and start all associated runnables
     * @param context
     * @param handler
     */
    void register(Context context, Handler handler);

    /**
     * Unregister thereceiver and stop all associated runnables
     * @param context
     * @return
     */
    boolean unregister(Context context);

    /**
     * Save all associated runnables prematurely in case the data is needed immediately
     */
    void savePrematurely();
}
