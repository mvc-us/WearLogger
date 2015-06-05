package com;

import com.dtw.FastDTW;
import com.timeseries.TimeSeries;
import com.util.DistanceFunction;
import com.util.DistanceFunctionFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by michael on 5/31/15.
 */
public class Knn {
    private TimeSeries[] trainingSet;
    private int k;
    private boolean[] positive; // true if is actual wanted gesture
    private static final int[] COL_TO_INCLUDE = {0,1,2,3};
    public static final int RADIUS = 20;

    public Knn (File[] trainingSetFiles, boolean[] positive, int k) {
        this.k = k;
        trainingSet = new TimeSeries[trainingSetFiles.length];
        for(int i = 0; i < trainingSetFiles.length; i++) {
            File file = trainingSetFiles[i];
            TimeSeries t = new TimeSeries(file, COL_TO_INCLUDE, true, false, ',');
            trainingSet[i] = t;
        }
        this.positive = new boolean[positive.length];
        System.arraycopy(positive, 0, this.positive, 0, positive.length);
    }

    public boolean classify(File file) {
        TimeSeries t = new TimeSeries(file, COL_TO_INCLUDE, true, false, ',');
        double[] distances = new double[trainingSet.length];
        final DistanceFunction distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance");
        for (int i = 0; i < trainingSet.length; i++) {
            distances[i] = FastDTW.getWarpDistBetween(t, trainingSet[i], RADIUS, distFn);
        }

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

    private static Collection<Double> findKSmallest(Collection<Double> data, int k) {
        assert k > 0;
        assert k <= data.size();
        ArrayList<Double> dataAsList = new ArrayList<Double>(data);
        Collections.sort(dataAsList);
        return dataAsList.subList(0, k);
    }

    private static double[] findKSmallest(double[] data, int k) {
        assert k > 0;
        assert k <= data.length;
        double[] dataCopy = data.clone();
        Arrays.sort(dataCopy);
        return Arrays.copyOfRange(dataCopy, 0, k);
    }
}
