package com.limvi_licef.ar_driving_assistant.algorithms;

import android.content.Context;

import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.timeseries.TimeSeriesBase;
import com.fastdtw.timeseries.TimeSeriesItem;
import com.fastdtw.timeseries.TimeSeriesPoint;
import com.limvi_licef.ar_driving_assistant.models.TimestampedDouble;
import com.limvi_licef.ar_driving_assistant.utils.Database;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension of the TimeSeriesBase that allows modification of the TimeSeries after the Builder created it
 */
public class TimeSeriesExtended implements TimeSeries {

    private List<TimeSeriesItem> items;
    private final int numDimensions;

    public TimeSeriesExtended(List<TimeSeriesItem> items) {
        this.items = items;
        this.numDimensions = items.isEmpty() ? 0 : items.get(0).getPoint().size();
    }

    /**
     * Add a element to the TimeSeries and removes the first element if the TimeSeries exceeds the max size
     * @param maxSize the max size of the TimeSeries
     * @param time the time of the element to add
     * @param values the values of the element to add
     */
    public void shiftRightByOne(int maxSize, double time, double... values) {
        if(items.size() >= maxSize && items.size() > 0) {
            items.remove(0);
        }
         items.add(new TimeSeriesItem(time, new TimeSeriesPoint(values)));
    }

    public final static TimeSeriesExtended.Builder builder() {
        return new TimeSeriesExtended.Builder();
    }

    public final static TimeSeriesExtended.Builder add(double time, double... values) {
        return builder().add(time, values);
    }

    public final static TimeSeriesExtended.Builder add(double time, TimeSeriesPoint point) {
        return builder().add(time, point);
    }

    public final static class Builder {

        private List<TimeSeriesItem> items = new ArrayList<TimeSeriesItem>() ;

        public TimeSeriesExtended.Builder add(double time, double... values) {
            items.add(new TimeSeriesItem(time, new TimeSeriesPoint(values)));
            return this;
        }

        public TimeSeriesExtended.Builder add(TimeSeriesItem item) {
            items.add(item);
            return this;
        }

        public TimeSeriesExtended.Builder add(double time, TimeSeriesPoint point) {
            items.add(new TimeSeriesItem(time, point));
            return this;
        }

        public TimeSeriesExtended build() {
            return new TimeSeriesExtended(items);
        }
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public int numOfDimensions() {
        return numDimensions;
    }

    @Override
    public double getTimeAtNthPoint(int n) {
        return items.get(n).getTime();
    }

    @Override
    public double getMeasurement(int pointIndex, int valueIndex) {
        return items.get(pointIndex).getPoint().get(valueIndex);
    }

    @Override
    public double[] getMeasurementVector(int pointIndex) {
        return items.get(pointIndex).getPoint().toArray();
    }

    /**
     * Creates a TimeSeriesExtended using given parameters
     * @param context
     * @param startTimestamp the timestamp from which to fetch data
     * @param endTimestamp the timestamp to which to fetch data
     * @param tableName the table containing the columns
     * @param valueColumnNames the varargs strings defining the columns to be used to create the time series
     * @return
     */
    public static TimeSeriesExtended createTimeSeriesFromSensor(Context context, long startTimestamp, long endTimestamp, String tableName, String... valueColumnNames) {
        Builder b = builder();

        List<List<TimestampedDouble>> valuesList = new ArrayList<>();
        for(String column : valueColumnNames) {
            valuesList.add(Database.getSensorData(startTimestamp, endTimestamp, tableName, column, context));
        }

        for(int i = 0; i < valuesList.get(0).size(); i++) {
            double [] values = new double[valueColumnNames.length];
            try {
                for(int j = 0; j < valueColumnNames.length; j++){
                    values[j] = valuesList.get(j).get(i).value;
                }
                b.add(valuesList.get(0).get(i).timestamp, values);
            } catch (IndexOutOfBoundsException e) {
                //skip
            }
        }
        return b.build();
    }
}
