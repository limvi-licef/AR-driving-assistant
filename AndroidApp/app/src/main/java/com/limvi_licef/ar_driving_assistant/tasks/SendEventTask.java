package com.limvi_licef.ar_driving_assistant.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.utils.Events;

import java.io.IOException;

public class SendEventTask extends AsyncTask<String, Void, String> {

    private Context context;
    private String message;
    private String eventType;

    public SendEventTask(Context context, String eventName, String message){
        this.context = context;
        this.message = message;
        this.eventType = eventName;
    }

    protected String doInBackground(String... urls) {
        try {
            if (!Events.sendEvent(context, eventType, message))
                return context.getResources().getString(R.string.send_event_task_invalid_ip);
            return context.getResources().getString(R.string.send_event_task_success);
        } catch (IOException e) {
            Log.d("EventSender", "" + e.getMessage());
            return context.getResources().getString(R.string.send_event_task_failure);
        }
    }

    protected void onPostExecute(String toast) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
    }
}
