package com.limvi_licef.ar_driving_assistant.utils;

import android.content.Context;

import com.fastdtw.timeseries.TimeSeriesBase;

import java.util.ArrayList;
import java.util.List;

public abstract class TimeSeries {

    public static com.fastdtw.timeseries.TimeSeries createTimeSeriesFromSensor(Context context, long startTimestamp, long endTimestamp, String tableName, String... valueColumnNames) {
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
}
