package com.limvi_licef.ar_driving_assistant.algorithms;

import com.limvi_licef.ar_driving_assistant.utils.Statistics;
import com.limvi_licef.ar_driving_assistant.utils.Structs.SegmentationAlgorithmReturnData;
import com.limvi_licef.ar_driving_assistant.utils.Structs.TimestampedDouble;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/*
 * Adapted from https://github.com/lemire/MonotoneSegment
 */

public class MonotoneSegmentationAlgorithm {

    public static SegmentationAlgorithmReturnData computeData(List<TimestampedDouble> values, int tolerance) {
        List<Integer> significantExtrema = selectSignificantExtrema(values, tolerance);
        return new SegmentationAlgorithmReturnData(Statistics.computeExtremaStats(values, significantExtrema), piecewiseMonotone(values, significantExtrema));
    }

    private static Map<Integer,Double> computeScaleLabels(List<TimestampedDouble> values) {
        if(values.size() < 3) return new HashMap<>();
        boolean isMin;
        Map<Integer,Double> extrema = new LinkedHashMap<>(values.size());
        Map<Integer,Double> scaleLabels = new HashMap<>(values.size());
        List<Integer> indexes = new ArrayList<>();
        int lastKey = 0;

        //select extrema
        extrema.put(0, values.get(0).value);
        for(int i = 1; i < values.size() - 1; i++) {
            if(values.get(i).equals(values.get(i-1))) {
                //do nothing
            } else if((values.get(i).value >= values.get(i-1).value) && (values.get(i).value >= values.get(i+1).value)) {
                extrema.put(i, values.get(i).value);
                lastKey= i;
            } else if((values.get(i).value <= values.get(i-1).value) && (values.get(i).value <= values.get(i+1).value)) {
                extrema.put(i, values.get(i).value);
                lastKey= i;
            }
        }
        if(values.get(values.size()-1).value.equals(extrema.get(lastKey))) {
            extrema.remove(lastKey);
            extrema.put(lastKey, values.get(values.size()-1).value);
        } else {
            extrema.put(lastKey + 1, values.get(values.size()-1).value);
        }

        //create scalelabel array
        Collection<Double> tempValues = extrema.values();
        if (tempValues.size() < 2) return new HashMap<>();
        Iterator it = tempValues.iterator();
        Double first = (Double)it.next();
        Double second = (Double)it.next();
        isMin = (first < second);
        for(Map.Entry<Integer,Double> extremum : extrema.entrySet()) {
            int i = extremum.getKey();
            while((indexes.size() > 2) && (( (!isMin) && (extrema.get(i) > extrema.get(indexes.get(1)))) || (isMin && (extrema.get(i) < extrema.get(indexes.get(1))))) ) {
                Double scale = Math.abs(extrema.get(indexes.get(1)) - extrema.get(indexes.get(0)));
                scaleLabels.put(indexes.get(0), scale);
                scaleLabels.put(indexes.get(1), scale);
                indexes.remove(1);
                indexes.remove(0);
            }
            if((indexes.size() == 2) && (( (!isMin) && (extrema.get(i) > extrema.get(indexes.get(1)))) || (isMin && (extrema.get(i) < extrema.get(indexes.get(1))))) ) {
                Double scale = Math.abs(extrema.get(indexes.get(1)) - extrema.get(indexes.get(0)));
                scaleLabels.put(indexes.get(1), scale);
                indexes.remove(1);
            }
            isMin = !isMin;
            indexes.add(0, i);
        }
        while(indexes.size() > 2) {
            Double scale = Math.abs(extrema.get(indexes.get(1)) - extrema.get(indexes.get(0)));
            scaleLabels.put(indexes.get(0), scale);
            indexes.remove(0);
        }
        Double scale = Math.abs(extrema.get(indexes.get(1)) - extrema.get(indexes.get(0)));
        scaleLabels.put(indexes.get(0), scale);
        scaleLabels.put(indexes.get(1), scale);
        indexes.clear();
        return scaleLabels;
    }

    private static List<Integer> selectSignificantExtrema(List<TimestampedDouble> values, int tolerance) {
        Map<Integer,Double> scaleLabels = computeScaleLabels(values);
        List<Integer> significantExtremaIndexes = new ArrayList<>();

        for(Map.Entry<Integer,Double> scaleLabel : scaleLabels.entrySet()) {
            if(scaleLabel.getValue() >= tolerance) {
                significantExtremaIndexes.add(scaleLabel.getKey());
            }
        }
        Collections.sort(significantExtremaIndexes);
        return significantExtremaIndexes;
    }

    private static List<TimestampedDouble> piecewiseMonotone(List<TimestampedDouble> values, List<Integer> indexes){
        Map<Integer,TimestampedDouble> monotoneValues = new TreeMap<>();
        for(int i = 0; i < indexes.size() - 1; ++ i) {
            Double a = values.get(indexes.get(i)).value;
            Double b = values.get(indexes.get(i+1)).value;
            Map<Integer,TimestampedDouble> Xmin = new HashMap<>();
            Map<Integer,TimestampedDouble> Xmax = new HashMap<>();

            if(a > b) {
                Xmin.put(indexes.get(i), values.get(indexes.get(i)));
                Xmax.put(indexes.get(i+1), values.get(indexes.get(i+1)));
                for(int j = indexes.get(i) + 1; j <= indexes.get(i+1); ++j) {
                    if( values.get(j).value < Xmin.get(j-1).value) {
                        Xmin.put(j, values.get(j));
                    } else {
                        Xmin.put(j, Xmin.get(j-1));
                    }
                }
                for(int j = indexes.get(i+1) - 1; j >= indexes.get(i); --j) {
                    if( values.get(j).value < Xmax.get(j+1).value) {
                        Xmax.put(j, Xmax.get(j+1));
                    } else {
                        Xmax.put(j, values.get(j));
                    }
                }
            } else {
                Xmin.put(indexes.get(i+1), values.get(indexes.get(i+1)));
                Xmax.put(indexes.get(i), values.get(indexes.get(i)));
                for(int j = indexes.get(i) + 1; j <= indexes.get(i+1); ++j) {
                    if( values.get(j).value > Xmax.get(j-1).value) {
                        Xmax.put(j, values.get(j));
                    } else {
                        Xmax.put(j, Xmax.get(j-1));
                    }
                }
                for(int j = indexes.get(i+1) - 1; j >= indexes.get(i); --j) {
                    if( values.get(j).value > Xmin.get(j+1).value) {
                        Xmin.put(j, Xmin.get(j+1));
                    } else {
                        Xmin.put(j, values.get(j));
                    }
                }
            }
            for (Map.Entry<Integer,TimestampedDouble> max : Xmax.entrySet()) {
                int key = max.getKey();
                monotoneValues.put(key, new TimestampedDouble((Xmax.get(key).timestamp + Xmin.get(key).timestamp) / 2L , (Xmax.get(key).value + Xmin.get(key).value) / 2.0));
            }
        }

        return new ArrayList<>(monotoneValues.values());
    }

}
