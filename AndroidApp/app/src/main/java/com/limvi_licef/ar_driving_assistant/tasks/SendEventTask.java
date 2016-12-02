package com.limvi_licef.ar_driving_assistant.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.network.TCPListenerThread;
import com.limvi_licef.ar_driving_assistant.utils.Config;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Send an event to the Unity app
 */
public class SendEventTask extends AsyncTask<String, Void, String> {

    private Context context;
    private String message;
    private String eventType;

    public SendEventTask(Context context, String eventName, String message){
        this.context = context;
        this.message = message;
        this.eventType = eventName;
    }

    /**
     * Build event json to be displayed on the Unity app then send it
     * @param urls
     * @return
     */
    protected String doInBackground(String... urls) {
        JSONObject json = new JSONObject();
        try {
            json.put(Config.HoloLens.JSON_REQUEST_TYPE, Config.HoloLens.JSON_REQUEST_TYPE_PARAM_EVENT);
            json.put(Config.HoloLens.JSON_EVENT_TYPE, eventType);
            json.put(Config.HoloLens.JSON_EVENT_MESSAGE, message);
        } catch (JSONException e) {
            return context.getResources().getString(R.string.send_event_task_failure);
        }
        return TCPListenerThread.sendJson(context, json);
    }

    /**
     * Show send JSON result as a toast
     * @param toast
     */
    protected void onPostExecute(String toast) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
    }
}
