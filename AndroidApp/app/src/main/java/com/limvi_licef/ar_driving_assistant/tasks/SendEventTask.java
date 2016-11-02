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

public class SendEventTask extends AsyncTask<String, Void, Boolean> {

    private Context context;
    private String message;
    private final static String delimiter = ";";

    public SendEventTask(Context context, String eventName, String message){
        this.context = context;
        this.message = new StringBuilder().append(eventName).append(delimiter).append(message).append("\r\n").toString();
    }

    protected Boolean doInBackground(String... urls) {
        try {
            InetAddress ipAddress = InetAddress.getByName(Preferences.getIPAddress(context));
            DatagramSocket socket = DatagramChannel.open().socket();
            DatagramPacket dp = new DatagramPacket(message.getBytes(), message.length(), ipAddress, Constants.HOLOLENS_PORT);
            socket.send(dp);
            socket.close();
            return true;
        } catch (UnknownHostException e) {
            Log.d("EventSender", "" + e.getMessage());
            return false;
        } catch (IOException e) {
            Log.d("EventSender", "" + e.getMessage());
            return false;
        }
    }

    protected void onPostExecute(Boolean result) {
        Toast.makeText(context, result ? context.getResources().getString(R.string.send_event_task_success) : context.getResources().getString(R.string.send_event_task_failure), Toast.LENGTH_SHORT).show();
    }
}
