package com.limvi_licef.ar_driving_assistant.algorithms;

/*
 * Implementation of the main algorithm in
 * Daniel Lemire, Martin Brooks and Yuhong Yan, An Optimal Linear Time Algorithm for Quasi-Monotonic Segmentation. International Journal of Computer Mathematics 86 (7), 2009.
 * http://arxiv.org/abs/0709.1166
 */

import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MonotoneSegmentationAlgorithm {

    public void main() {
        ArrayList<Double> values = new ArrayList<>();
        ArrayList<Integer> toleranceTest = new ArrayList<>();

        for(int i : toleranceTest) {
            ArrayList<Double> monotoneValues = piecewiseMonotone(values, selectSignificantExtrema(values, i));
            for(Double value : monotoneValues){
                Log.d("Monotone values", value.toString());
            }
        }
    }

    private ArrayList<Integer> computeScaleLabels(ArrayList<Double> X) {
        ArrayList<Integer> scaleLabels = new ArrayList<>();
        return scaleLabels;
    }

    private ArrayList<Integer> selectSignificantExtrema(ArrayList<Double> X, int tolerance) {
        ArrayList<Integer> scaleLabel = computeScaleLabels(X);
        ArrayList<Integer> answer = new ArrayList<>();
        for(int i : scaleLabel) {
            if(scaleLabel.get(i) >= tolerance) {
                answer.add(i);
            }
        }
        return answer;
    }

    private ArrayList<Double> piecewiseMonotone(ArrayList<Double> X, ArrayList<Integer> indexes){
        ArrayList<Double> answer = new ArrayList<>();
        for(int i = 0; i < indexes.size() - 1; ++ i) {
            Double a = X.get(indexes.get(i));
            Double b = X.get(indexes.get(i+1));
            ArrayList<Double> Xmin = new ArrayList<>();
            ArrayList<Double> Xmax = new ArrayList<>();

            if(a > b) {
                Log.d("Monotone Algorithm", "Decreasing from " + a + " to " + b + " starting at " + indexes.get(i));
                Xmin.add(indexes.get(i), X.get(indexes.get(i)));
                Xmax.add(indexes.get(i+1), X.get(indexes.get(i+1)));
                for(int j = indexes.get(i) + 1; j <= indexes.get(i+1); ++j) {
                    if( X.get(j) < Xmin.get(j-1)) {
                        Xmin.add(j, X.get(j));
                    } else {
                        Xmin.add(j, Xmin.get(j-1));
                    }
                }
                for(int j = indexes.get(i+1) - 1; j >= indexes.get(i); --j) {
                    if( X.get(j) < Xmax.get(j+1)) {
                        Xmax.add(j, Xmax.get(j+1));
                    } else {
                        Xmax.add(j, X.get(j));
                    }
                }
            } else {
                Log.d("Monotone Algorithm", "increasing from " + a + " to " + b + " starting at " + indexes.get(i));
                Xmin.add(indexes.get(i+1), X.get(indexes.get(i+1)));
                Xmax.add(indexes.get(i), X.get(indexes.get(i)));
                for(int j = indexes.get(i) + 1; j <= indexes.get(i+1); ++j) {
                    if( X.get(j) > Xmax.get(j-1)) {
                        Xmax.add(j, X.get(j));
                    } else {
                        Xmax.add(j, Xmax.get(j-1));
                    }
                }
                for(int j = indexes.get(i+1) - 1; j >= indexes.get(i); --j) {
                    if( X.get(j) > Xmin.get(j+1)) {
                        Xmin.add(j, Xmin.get(j+1));
                    } else {
                        Xmin.add(j, X.get(j));
                    }
                }
            }

            for (Double j : Xmax) {
                int index = Xmax.indexOf(j);
                answer.add((Xmax.get(index) + Xmin.get(index)) / 2.0);
            }
        }
        return answer;
    }

}



