package com.limvi_licef.ar_driving_assistant.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.timeseries.TimeSeriesBase;
import com.limvi_licef.ar_driving_assistant.database.DatabaseContract;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;

public final class Events {

    private Events(){}

    private static final String DEFAULT_EVENT_TYPE = "Information";
    private final static String delimiter = ";";

    public static boolean sendEvent(Context context, String eventType, String msgFragment) throws IOException {
        String message = (eventType != null ? eventType : DEFAULT_EVENT_TYPE) + delimiter + msgFragment + "\r\n";
        String ipString = Preferences.getIPAddress(context);
        if (ipString == null || ipString.isEmpty()) return false;

        InetAddress ipAddress = InetAddress.getByName(ipString);
        DatagramSocket socket = DatagramChannel.open().socket();
        DatagramPacket dp = new DatagramPacket(message.getBytes(), message.length(), ipAddress, Constants.HOLOLENS_PORT);
        socket.send(dp);
        socket.close();
        return true;
    }

    public static TimeSeries createTimeSeriesFromSensor(Context context, long startTimestamp, long endTimestamp, String tableName, String... valueColumnNames) {
        List<List<Structs.TimestampedDouble>> valuesList = new ArrayList<>();
        for(String column : valueColumnNames) {
            valuesList.add(Database.getSensorData(startTimestamp, endTimestamp, tableName, column, context));
        }

        TimeSeriesBase.Builder b = TimeSeriesBase.builder();
        for(int i = 0; i < valuesList.get(0).size(); i++) {
            double [] values = new double[valueColumnNames.length];
            for(int j = 0; j < valueColumnNames.length; j++){
                values[j] = valuesList.get(j).get(i).value;
            }
            b.add(valuesList.get(0).get(i).timestamp, values);
        }
        return b.build();
    }

    public static String saveNewEvent(Context context, SQLiteDatabase db, long startTimestamp, long endTimestamp, String label) {
        db.beginTransaction();
        String userId = Preferences.getCurrentUserId(context);
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.TrainingEvents.CURRENT_USER_ID, userId);
        values.put(DatabaseContract.TrainingEvents.START_TIMESTAMP, startTimestamp);
        values.put(DatabaseContract.TrainingEvents.END_TIMESTAMP, endTimestamp);
        values.put(DatabaseContract.TrainingEvents.LABEL, label);
        int result = (int) db.insertWithOnConflict(DatabaseContract.TrainingEvents.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.setTransactionSuccessful();
        db.endTransaction();
        return result == -1 ? "Label already exists" : "No Match Found, inserting event to database";
    }
}
