package com.michaelchen.wearlogger;

import com.timeseries.TimeSeries;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by michael on 6/7/15.
 */
public class KnnClassifier extends GestureClassifier {
    static int k = 3;
    public KnnClassifier(File[] trainingSetFiles, boolean[] positive) {
        super(trainingSetFiles, positive);
    }

    @Override
    void initialize() {

    }

    @Override
    public boolean runClassification(TimeSeries toEval, double[] distances) {
        HashMap<Double, Integer> distanceToIndex = new HashMap<>(distances.length);
        for (int i = 0; i < distances.length; i++) {
            distanceToIndex.put(distances[i], i);
        }

        double[] closest = findKSmallest(distances, k);
        int countTrue = 0;

        for (double distance : closest) {
            int index = distanceToIndex.get(distance);
            countTrue += positive[index] ? 1 : 0;
        }

        return countTrue > (k/2); // if requires strong majority, if k is even, equal does not pass
    }

    private static double[] findKSmallest(double[] data, int k) {
        assert k > 0;
        assert k <= data.length;
        double[] dataCopy = data.clone();
        Arrays.sort(dataCopy);
        return Arrays.copyOfRange(dataCopy, 0, k);
    }
}
