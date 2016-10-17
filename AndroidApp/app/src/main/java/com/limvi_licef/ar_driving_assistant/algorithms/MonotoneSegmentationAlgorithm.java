package com.limvi_licef.ar_driving_assistant.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
 * Adapted from https://github.com/lemire/MonotoneSegment
 */

public class MonotoneSegmentationAlgorithm {

    public List<Double> ComputeData(List<Double> values, int tolerance) {
        List<Integer> significantExtrema = selectSignificantExtrema(values, tolerance);
        List<Double> monotoneValues = piecewiseMonotone(values, significantExtrema);
        return monotoneValues;
    }

    private Map<Integer,Double> computeScaleLabels(List<Double> values) {
        boolean isMin;
        Map<Integer,Double> extrema = new HashMap<>(values.size());
        Map<Integer,Double> scaleLabels = new HashMap<>(values.size());
        List<Integer> indexes = new ArrayList<>();
        int lastKey = 0;

        //select extrema
        extrema.put(0, values.get(0));
        for(int i = 1; i < values.size() - 1; i++) {
            if(values.get(i).equals(values.get(i-1))) {
                //do nothing
            } else if((values.get(i) >= values.get(i-1)) && (values.get(i) >= values.get(i+1))) {
                extrema.put(i, values.get(i));
                lastKey= i;
            } else if((values.get(i) <= values.get(i-1)) && (values.get(i) <= values.get(i+1))) {
                extrema.put(i, values.get(i));
                lastKey= i;
            }
        }
        if(values.get(values.size()-1).equals(extrema.get(lastKey))) {
            extrema.remove(lastKey);
            extrema.put(lastKey, values.get(values.size()-1));
        } else {
            extrema.put(lastKey + 1, values.get(values.size()-1));
        }

        //create scalelabel array
        isMin = (extrema.get(0) < extrema.get(1));
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

    private List<Integer> selectSignificantExtrema(List<Double> values, int tolerance) {
        Map<Integer,Double> scaleLabels = computeScaleLabels(values);
        List<Integer> significantExtremaIndexes = new ArrayList<>();

        for(Map.Entry<Integer,Double> scaleLabel : scaleLabels.entrySet()) {
            if(scaleLabel.getValue() >= tolerance) {
                significantExtremaIndexes.add(scaleLabel.getKey());
            }
        }
        return significantExtremaIndexes;
    }

    private List<Double> piecewiseMonotone(List<Double> values, List<Integer> indexes){
        Map<Integer,Double> monotoneValues = new LinkedHashMap<>();
        for(int i = 0; i < indexes.size() - 1; ++ i) {
            Double a = values.get(indexes.get(i));
            Double b = values.get(indexes.get(i+1));
            Map<Integer,Double> Xmin = new LinkedHashMap<>();
            Map<Integer,Double> Xmax = new LinkedHashMap<>();

            if(a > b) {
                Xmin.put(indexes.get(i), values.get(indexes.get(i)));
                Xmax.put(indexes.get(i+1), values.get(indexes.get(i+1)));
                for(int j = indexes.get(i) + 1; j <= indexes.get(i+1); ++j) {
                    if( values.get(j) < Xmin.get(j-1)) {
                        Xmin.put(j, values.get(j));
                    } else {
                        Xmin.put(j, Xmin.get(j-1));
                    }
                }
                for(int j = indexes.get(i+1) - 1; j >= indexes.get(i); --j) {
                    if( values.get(j) < Xmax.get(j+1)) {
                        Xmax.put(j, Xmax.get(j+1));
                    } else {
                        Xmax.put(j, values.get(j));
                    }
                }
            } else {
                Xmin.put(indexes.get(i+1), values.get(indexes.get(i+1)));
                Xmax.put(indexes.get(i), values.get(indexes.get(i)));
                for(int j = indexes.get(i) + 1; j <= indexes.get(i+1); ++j) {
                    if( values.get(j) > Xmax.get(j-1)) {
                        Xmax.put(j, values.get(j));
                    } else {
                        Xmax.put(j, Xmax.get(j-1));
                    }
                }
                for(int j = indexes.get(i+1) - 1; j >= indexes.get(i); --j) {
                    if( values.get(j) > Xmin.get(j+1)) {
                        Xmin.put(j, Xmin.get(j+1));
                    } else {
                        Xmin.put(j, values.get(j));
                    }
                }
            }
            for (Map.Entry<Integer,Double> max : Xmax.entrySet()) {
                int key = max.getKey();
                monotoneValues.put(key, (Xmax.get(key) + Xmin.get(key)) / 2.0);
            }
        }

        //reorder values
        List<Double> returnArray = new ArrayList<>();
        for(int i = 0; i < monotoneValues.size(); i++){
            returnArray.add(monotoneValues.get(i));
        }
        return returnArray;
    }

}
