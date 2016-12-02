package com.limvi_licef.ar_driving_assistant.network;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.limvi_licef.ar_driving_assistant.R;
import com.limvi_licef.ar_driving_assistant.config.Communication;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;
import com.limvi_licef.ar_driving_assistant.database.DatabaseHelper;
import com.limvi_licef.ar_driving_assistant.utils.Broadcasts;
import com.limvi_licef.ar_driving_assistant.utils.Preferences;
import com.limvi_licef.ar_driving_assistant.utils.Structs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Thread that listens for json messages from the Unity app
 */
public class TCPListenerThread extends Thread {

    /**
     * Error messages sent to UI in case of error
     */
    private static final String UNKNOWN_REQUEST = "Received Unknown Request";
    private static final String THREAD_END = "TCP Listener Ended";
    private static final String THREAD_EXCEPTION = "TCP Listener Exception ";
    private static final String RETURN_ERROR = "There was an error while returning data";
    private static final String SEND_ERROR = "There was an error while sending data";
    private static final String FETCH_USERS_ERROR = "There was an error while fetching all Users";
    private static final String INSERT_USER_ERROR = "There was an error while inserting a new user";
    private static final String FETCH_RIDES_ERROR = "There was an error while fetching user rides";

    private ServerSocket serverSocket;
    private Context context;
    private boolean running;

    public TCPListenerThread(Context context) {
        super();
        this.context = context;
    }

    public void kill() {
        this.running = false;
    }

    @Override
    public void run() {
        String message;
        running = true;

        try {
            serverSocket = new ServerSocket(Communication.HOLOLENS_PORT);

            while(running){

                // receive request
                Socket connectionSocket = serverSocket.accept();
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                message = inFromClient.readLine();

                try {
                    Log.d("TCP", message);
                    JSONObject request = new JSONObject(message);
                    handleRequest(request);
                } catch(JSONException e) {
                    Broadcasts.sendWriteToUIBroadcast(context, THREAD_EXCEPTION + e.getMessage());
                }
                connectionSocket.close();
            }
            Broadcasts.sendWriteToUIBroadcast(context, THREAD_END);
        } catch (SocketException e) {
            Broadcasts.sendWriteToUIBroadcast(context, THREAD_EXCEPTION + e.getMessage());
        } catch (IOException e) {
            Broadcasts.sendWriteToUIBroadcast(context, THREAD_EXCEPTION + e.getMessage());
        } finally {
            if(serverSocket != null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    Broadcasts.sendWriteToUIBroadcast(context, THREAD_EXCEPTION + e.getMessage());
                }
            }
        }
    }

    /**
     * Redirects the request to appropriate method
     * @param request the request to process
     * @throws JSONException
     */
    private void handleRequest(JSONObject request) throws JSONException {
        String requestType = request.getString(Communication.JSON_REQUEST_TYPE);
        Log.d("TCP", "Received request : " + requestType);
        JSONObject json;
        switch (requestType) {
            case Communication.JSON_REQUEST_TYPE_USERS:
                json = fetchUsers();
                break;
            case Communication.JSON_REQUEST_TYPE_INSERT_USER:
                json = insertNewUser(request);
                break;
            case Communication.JSON_REQUEST_TYPE_LAST_KNOWN:
                json = fetchAndSendLastKnownRides(request);
                break;
            default:
                Broadcasts.sendWriteToUIBroadcast(context, UNKNOWN_REQUEST);
                return;
        }
        sendJson(context, json);
    }

    /**
     * Fetches the users list
     * @return the response json
     */
    private JSONObject fetchUsers(){
        List<Structs.User> usersList = getAllUsers(context);
        JSONObject jsonResponse = new JSONObject();
        try {
            JSONArray users = new JSONArray();
            for(Structs.User user : usersList) {
                JSONObject jsonUser = new JSONObject();
                jsonUser.put(Communication.JSON_REQUEST_RETURN_VALUES_NAME, user.userName);
                jsonUser.put(Communication.JSON_REQUEST_RETURN_VALUES_AGE, user.userAge);
                jsonUser.put(Communication.JSON_REQUEST_RETURN_VALUES_GENDER, user.userGender);
                jsonUser.put(Communication.JSON_REQUEST_RETURN_VALUES_AVATAR, user.userAvatar);
                users.put(jsonUser);
            }
            jsonResponse.put(Communication.JSON_REQUEST_TYPE, Communication.JSON_REQUEST_TYPE_PARAM_USER);
            jsonResponse.put(Communication.JSON_USERS, users);
        } catch (JSONException ex) {
            Broadcasts.sendWriteToUIBroadcast(context, FETCH_USERS_ERROR);
        }
        return jsonResponse;
    }

    /**
     * Writes new user to database and sends it back to Unity app if insertion is successful
     * @param request request containing the new user's data
     * @return the response json
     */
    private JSONObject insertNewUser(JSONObject request){
        boolean status = true;
        JSONObject jsonResponse;
        try {
            SQLiteDatabase db = DatabaseHelper.getHelper(context).getWritableDatabase();
            ContentValues user = new ContentValues();
            user.put(DatabaseContract.Users.USER_NAME, request.getString(Communication.JSON_REQUEST_RETURN_VALUES_NAME));
            user.put(DatabaseContract.Users.USER_AGE, request.getInt(Communication.JSON_REQUEST_RETURN_VALUES_AGE));
            user.put(DatabaseContract.Users.USER_GENDER, request.getString(Communication.JSON_REQUEST_RETURN_VALUES_GENDER));
            user.put(DatabaseContract.Users.USER_AVATAR, request.getInt(Communication.JSON_REQUEST_RETURN_VALUES_AVATAR));
            status = db.insert(DatabaseContract.Users.TABLE_NAME, null, user) != -1;
        } catch(JSONException e) {
            status = false;
        } finally {
            jsonResponse = new JSONObject();
            try {
                JSONObject newUser = new JSONObject();
                newUser.put(Communication.JSON_REQUEST_RETURN_VALUES_NAME, request.getString(Communication.JSON_REQUEST_RETURN_VALUES_NAME));
                newUser.put(Communication.JSON_REQUEST_RETURN_VALUES_AGE, request.getInt(Communication.JSON_REQUEST_RETURN_VALUES_AGE));
                newUser.put(Communication.JSON_REQUEST_RETURN_VALUES_GENDER, request.getString(Communication.JSON_REQUEST_RETURN_VALUES_GENDER));
                newUser.put(Communication.JSON_REQUEST_RETURN_VALUES_AVATAR, request.getInt(Communication.JSON_REQUEST_RETURN_VALUES_AVATAR));

                jsonResponse.put(Communication.JSON_REQUEST_TYPE, Communication.JSON_REQUEST_TYPE_PARAM_NEW_USER);
                jsonResponse.put(Communication.JSON_REQUEST_TYPE_PARAM_NEW_USER, newUser);
                jsonResponse.put(Communication.JSON_RETURN_STATUS, status);
            } catch (JSONException ex) {
                if(!status) {
                    Broadcasts.sendWriteToUIBroadcast(context, INSERT_USER_ERROR);
                } else {
                    Broadcasts.sendWriteToUIBroadcast(context, RETURN_ERROR);
                }
            }
        }
        return jsonResponse;
    }

    /**
     * Fetches an user's last known rides and sends them to the Unity app
     * @param request the request containing the user id to look for
     * @return the response json
     */
    private JSONObject fetchAndSendLastKnownRides(JSONObject request){
        String status = "";
        List<String> rides = new ArrayList<>();
        JSONObject jsonResponse;
        try {
            String userId = request.getString(Communication.JSON_REQUEST_ID);
            rides = getUserLastKnownRides(userId);
            if(rides.isEmpty()) {
                status = Communication.JSON_LAST_KNOWN_EMPTY_RESULT;
            } else {
                status = Communication.JSON_LAST_KNOWN_SUCCESS;
            }
        } catch (JSONException e) {
            status = Communication.JSON_LAST_KNOWN_ERROR_MESSAGE;
        } finally {
            jsonResponse = new JSONObject();
            try {
                jsonResponse.put(Communication.JSON_REQUEST_TYPE, Communication.JSON_REQUEST_TYPE_PARAM_RIDES);
                jsonResponse.put(Communication.JSON_RIDES, new JSONArray(rides));
                jsonResponse.put(Communication.JSON_RETURN_STATUS, status);
            } catch (JSONException ex) {
                Broadcasts.sendWriteToUIBroadcast(context, FETCH_RIDES_ERROR);
            }
        }
        return jsonResponse;
    }

    /**
     * Fetches all users
     * @param context
     * @return users
     */
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

    /**
     * Fetches an user's last known rides
     * @param id the user id to
     * @return a list of strings containing dates
     */
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

            //add only unique dates
            if (!date.equals(lastDate)) {
                rides.add(date);
            }
            lastDate = date;
        }
        ridesCursor.close();
        return rides;
    }

    /**
     * Sends a JSON string to the UnityApp
     * @param context
     * @param data the json string to send
     * @return the status string
     */
    public static String sendJson(Context context, JSONObject data) {
        try {
            String message = data.toString();
            Log.d("TCP", message);
            String ipString = Preferences.getIPAddress(context);
            if (ipString == null || ipString.isEmpty()) return context.getResources().getString(R.string.send_event_task_invalid_ip);

            InetAddress ipAddress = InetAddress.getByName(ipString);
            Socket socket = new Socket(ipAddress, Communication.HOLOLENS_PORT);
            DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
            outToServer.write(message.getBytes("UTF-8"));
            outToServer.flush();
            outToServer.close();
            socket.close();
        } catch (IOException e) {
            Log.d("EventSender", "" + e.getMessage());
            Broadcasts.sendWriteToUIBroadcast(context, SEND_ERROR);
            return context.getResources().getString(R.string.send_event_task_failure);
        }
        return context.getResources().getString(R.string.send_event_task_success);
    }
}
