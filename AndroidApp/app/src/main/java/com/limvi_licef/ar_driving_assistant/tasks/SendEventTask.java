package com.limvi_licef.ar_driving_assistant.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.utils.Constants;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;

public class SendEventTask extends AsyncTask<String, Void, String> {

    private Context context;
    private String message;
    private final static String delimiter = ";";

    public SendEventTask(Context context, String eventName, String message){
        this.context = context;
        this.message = eventName + delimiter + message + "\r\n";
    }

    protected String doInBackground(String... urls) {
        try {
            InetAddress ipAddress = InetAddress.getByName(Preferences.getIPAddress(context));
            DatagramSocket socket = DatagramChannel.open().socket();
            DatagramPacket dp = new DatagramPacket(message.getBytes(), message.length(), ipAddress, Constants.HOLOLENS_PORT);
            socket.send(dp);
            socket.close();
            return context.getResources().getString(R.string.send_event_task_success);
        } catch (UnknownHostException e) {
            Log.d("EventSender", "" + e.getMessage());
            return context.getResources().getString(R.string.send_event_task_invalid_ip);
        } catch (IOException e) {
            Log.d("EventSender", "" + e.getMessage());
            return context.getResources().getString(R.string.send_event_task_failure);
        }
    }

    protected void onPostExecute(String toast) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
    }
}
