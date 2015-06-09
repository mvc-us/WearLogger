package com.michaelchen.wearlogger;

import com.dtw.FastDTW;
import com.timeseries.TimeSeries;
import com.util.DistanceFunction;
import com.util.DistanceFunctionFactory;

import java.io.File;

/**
 * Created by michael on 6/8/15.
 */
public class DistanceClassifier extends GestureClassifier {

    protected double maxInternalDistance;
    protected TimeSeries[] correctTimeSeries;
    protected static final DistanceFunction distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance");

    public DistanceClassifier(File[] trainingSetFiles, boolean[] positive) {
        super(trainingSetFiles, positive);
    }

    private static int countNumPositive(boolean[] positive) {
        int count = 0;
        for (boolean b : positive) {
            count += b ? 1 : 0;
        }
        return count;
    }

    private static double computeMaxPairwiseDistances(TimeSeries[] timeSeries) {
        double maxValue = Double.MIN_VALUE;

        for (int i = 0; i < timeSeries.length - 1; i++) {
            for (int j = i+1; j < timeSeries.length; j++) {
                double distance = FastDTW.getWarpDistBetween(timeSeries[i], timeSeries[j], RADIUS, distFn);
                maxValue = distance > maxValue ? distance : maxValue;
            }
        }
        return maxValue;
    }

    @Override
    void initialize() {
        int numPositive = countNumPositive(positive);
        int serIndex = 0;
        correctTimeSeries = new TimeSeries[numPositive];
        for (int i = 0; i < trainingSet.length; i++) {
            if (positive[i]) {
                correctTimeSeries[serIndex] = trainingSet[i];
                serIndex++;
            }
        }
        maxInternalDistance = computeMaxPairwiseDistances(correctTimeSeries);
    }

    @Override
    public boolean runClassification(TimeSeries toEval, double[] distances) {
        // all distances must be closer than maxInternalDistance
        for (TimeSeries t : correctTimeSeries) {
            double distance = FastDTW.getWarpDistBetween(toEval, t, RADIUS, distFn);
            if (distance > maxInternalDistance) return false;
        }
        return true;
    }
}
