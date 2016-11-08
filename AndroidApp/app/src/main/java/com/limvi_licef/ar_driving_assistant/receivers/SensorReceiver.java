package com.limvi_licef.ar_driving_assistant.receivers;

import android.content.Context;
import android.os.Handler;

public interface SensorReceiver {
    void register(Context context, Handler handler);
    boolean unregister(Context context);
    void savePrematurely();
}
