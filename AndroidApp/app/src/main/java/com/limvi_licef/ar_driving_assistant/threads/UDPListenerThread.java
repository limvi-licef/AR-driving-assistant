package com.limvi_licef.ar_driving_assistant.threads;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Config;
import com.limvi_licef.ar_driving_assistant.utils.Events;
import com.limvi_licef.ar_driving_assistant.utils.Structs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class UDPListenerThread extends Thread {

    private static final String UNKNOWN_REQUEST = "Received Unknown Request";
    private static final String THREAD_END = "UDP Listener Ended";
    private static final String THREAD_EXCEPTION = "UDP Listener Exception ";
    private static final String FETCH_USERS_ERROR = "There was an error while fetching all Users";
    private static final String FETCH_RIDES_ERROR = "There was an error while fetching user rides";

    private DatagramSocket socket;
    private Context context;
    private boolean running;

    public UDPListenerThread(Context context) {
        super();
        this.context= context;
    }

    public void kill() {
        this.running = false;
    }

    @Override
    public void run() {
        String message;
        running = true;

        try {
            socket = new DatagramSocket(Config.HoloLens.HOLOLENS_PORT);

            while(running){
                byte[] buffer = new byte[1024];

                // receive request
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                message = new String(buffer, 0, packet.getLength());
                try {
                    Log.d("UDPListener", message);
                    JSONObject request = new JSONObject(message);
                    handleRequest(request);
                } catch(JSONException e) {
                    Broadcasts.sendWriteToUIBroadcast(context, THREAD_EXCEPTION + e.getMessage());
                }
            }
            Broadcasts.sendWriteToUIBroadcast(context, THREAD_END);
        } catch (SocketException e) {
            Broadcasts.sendWriteToUIBroadcast(context, THREAD_EXCEPTION + e.getMessage());
        } catch (IOException e) {
            Broadcasts.sendWriteToUIBroadcast(context, THREAD_EXCEPTION + e.getMessage());
        } finally {
            if(socket != null){
                socket.close();
            }
        }
    }

    private void handleRequest(JSONObject request) throws JSONException{
        String requestType = request.getString(Config.HoloLens.JSON_REQUEST_TYPE);
        Log.d("UDPListener", "Received request : " + requestType);
        if(requestType.equals(Config.HoloLens.JSON_REQUEST_TYPE_USERS)) {
            fetchAndSendUsers();
        } else if (requestType.equals(Config.HoloLens.JSON_REQUEST_TYPE_INSERT_USER)) {
            insertNewUser(request);
        } else if (requestType.equals(Config.HoloLens.JSON_REQUEST_TYPE_LAST_KNOWN)) {
            fetchAndSendLastKnownRides(request);
        } else {
            Broadcasts.sendWriteToUIBroadcast(context, UNKNOWN_REQUEST);
        }
    }

    private void fetchAndSendUsers(){
        List<Structs.User> usersList = getAllUsers(context);
        JSONObject json = new JSONObject();
        try {
            JSONArray users = new JSONArray();
            for(Structs.User user : usersList) {
                JSONObject jsonUser = new JSONObject();
                jsonUser.put(Config.HoloLens.JSON_REQUEST_RETURN_VALUES_NAME, user.userName);
                jsonUser.put(Config.HoloLens.JSON_REQUEST_RETURN_VALUES_AGE, user.userAge);
                jsonUser.put(Config.HoloLens.JSON_REQUEST_RETURN_VALUES_GENDER, user.userGender);
                jsonUser.put(Config.HoloLens.JSON_REQUEST_RETURN_VALUES_AVATAR, user.userAvatar);
                users.put(jsonUser);
            }
            json.put(Config.HoloLens.JSON_REQUEST_TYPE, Config.HoloLens.JSON_REQUEST_TYPE_PARAM_USER);
            json.put(Config.HoloLens.JSON_USERS, users);
        } catch (JSONException ex) {
            Broadcasts.sendWriteToUIBroadcast(context, FETCH_USERS_ERROR);
        }
        Events.sendJson(context, json);
    }

    private void insertNewUser(JSONObject request){
        boolean status = true;
        try {
            SQLiteDatabase db = DatabaseHelper.getHelper(context).getWritableDatabase();
            ContentValues user = new ContentValues();
            user.put(DatabaseContract.Users.USER_NAME, request.getString(Config.HoloLens.JSON_REQUEST_RETURN_VALUES_NAME));
            user.put(DatabaseContract.Users.USER_AGE, request.getInt(Config.HoloLens.JSON_REQUEST_RETURN_VALUES_AGE));
            user.put(DatabaseContract.Users.USER_GENDER, request.getString(Config.HoloLens.JSON_REQUEST_RETURN_VALUES_GENDER));
            user.put(DatabaseContract.Users.USER_AVATAR, request.getInt(Config.HoloLens.JSON_REQUEST_RETURN_VALUES_AVATAR));
            status = db.insert(DatabaseContract.Users.TABLE_NAME, null, user) != -1;
        } catch(JSONException e) {
            status = false;
        } finally {
            JSONObject json = new JSONObject();
            try {
                JSONObject newUser = new JSONObject();
                newUser.put(Config.HoloLens.JSON_REQUEST_RETURN_VALUES_NAME, request.getString(Config.HoloLens.JSON_REQUEST_RETURN_VALUES_NAME));
                newUser.put(Config.HoloLens.JSON_REQUEST_RETURN_VALUES_AGE, request.getInt(Config.HoloLens.JSON_REQUEST_RETURN_VALUES_AGE));
                newUser.put(Config.HoloLens.JSON_REQUEST_RETURN_VALUES_GENDER, request.getString(Config.HoloLens.JSON_REQUEST_RETURN_VALUES_GENDER));
                newUser.put(Config.HoloLens.JSON_REQUEST_RETURN_VALUES_AVATAR, request.getInt(Config.HoloLens.JSON_REQUEST_RETURN_VALUES_AVATAR));

                json.put(Config.HoloLens.JSON_REQUEST_TYPE, Config.HoloLens.JSON_REQUEST_TYPE_PARAM_NEW_USER);
                json.put(Config.HoloLens.JSON_REQUEST_TYPE_PARAM_NEW_USER, newUser);
                json.put(Config.HoloLens.JSON_RETURN_STATUS, status);
            } catch (JSONException ex) {
                Broadcasts.sendWriteToUIBroadcast(context, FETCH_USERS_ERROR);
            }
            Events.sendJson(context, json);
        }
    }

    private void fetchAndSendLastKnownRides(JSONObject request){
        String status = "";
        List<String> rides = new ArrayList<>();
        try {
            String userId = request.getString(Config.HoloLens.JSON_REQUEST_RETURN_VALUES_NAME);
            rides = getUserLastKnownRides(userId);
            if(rides.isEmpty()) {
                status = Config.HoloLens.JSON_LAST_KNOWN_EMPTY_RESULT;
            } else {
                status = Config.HoloLens.JSON_LAST_KNOWN_SUCCESS;
            }
        } catch (JSONException e) {
            status = Config.HoloLens.JSON_LAST_KNOWN_ERROR_MESSAGE;
        } finally {
            JSONObject json = new JSONObject();
            try {
                json.put(Config.HoloLens.JSON_REQUEST_TYPE, Config.HoloLens.JSON_REQUEST_TYPE_PARAM_RIDES);
                json.put(Config.HoloLens.JSON_RIDES, rides);
                json.put(Config.HoloLens.JSON_RETURN_STATUS, status);
            } catch (JSONException ex) {
                Broadcasts.sendWriteToUIBroadcast(context, FETCH_RIDES_ERROR);
            }
            Events.sendJson(context, json);
        }
    }

    private List<Structs.User> getAllUsers(Context context){
        List<Structs.User> users = new ArrayList<>();
        Cursor eventCursor = DatabaseHelper.getHelper(context).getReadableDatabase().query(DatabaseContract.Users.TABLE_NAME,
                new String[]{DatabaseContract.Users.USER_NAME,
                        DatabaseContract.Users.USER_AGE,
                        DatabaseContract.Users.USER_GENDER,
                        DatabaseContract.Users.USER_AVATAR},
                null, null, null, null, null);
        int nameColumnIndex = eventCursor.getColumnIndexOrThrow(DatabaseContract.Users.USER_NAME);
        int ageColumnIndex = eventCursor.getColumnIndexOrThrow(DatabaseContract.Users.USER_AGE);
        int genderColumnIndex = eventCursor.getColumnIndexOrThrow(DatabaseContract.Users.USER_GENDER);
        int avatarColumnIndex = eventCursor.getColumnIndexOrThrow(DatabaseContract.Users.USER_AVATAR);

        while (eventCursor.moveToNext()) {
            users.add(new Structs.User(eventCursor.getString(nameColumnIndex), eventCursor.getInt(ageColumnIndex), eventCursor.getString(genderColumnIndex),
                    eventCursor.getInt(avatarColumnIndex)));
        }
        eventCursor.close();

        return users;
    }

    private List<String> getUserLastKnownRides(String id) {
        List<String> rides = new ArrayList<>();
        Cursor ridesCursor = DatabaseHelper.getHelper(context).getReadableDatabase().query(DatabaseContract.LocationData.TABLE_NAME,
                new String[]{DatabaseContract.LocationData.TIMESTAMP},
                DatabaseContract.LocationData.CURRENT_USER_ID + " = ?", new String[]{"" + id}, null, null, "timestamp ASC");
        int timestampColumnIndex = ridesCursor.getColumnIndexOrThrow(DatabaseContract.LocationData.TIMESTAMP);
        String lastDate = "";
        while (ridesCursor.moveToNext()) {
            long timestamp = ridesCursor.getLong(timestampColumnIndex);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime());
            if (!date.equals(lastDate)) {
                rides.add(date);
            }
            lastDate = date;
        }
        ridesCursor.close();
        return rides;
    }
}
