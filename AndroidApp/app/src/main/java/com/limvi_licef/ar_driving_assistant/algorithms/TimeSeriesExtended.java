package com.limvi_licef.ar_driving_assistant.algorithms;

import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.timeseries.TimeSeriesBase;
import com.fastdtw.timeseries.TimeSeriesItem;
import com.fastdtw.timeseries.TimeSeriesPoint;

import java.util.ArrayList;
import java.util.List;

public class TimeSeriesExtended implements TimeSeries {

    private List<TimeSeriesItem> items;
    private final int numDimensions;

    public TimeSeriesExtended(List<TimeSeriesItem> items) {
        this.items = items;
        this.numDimensions = items.isEmpty() ? 0 : items.get(0).getPoint().size();
    }

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
}
