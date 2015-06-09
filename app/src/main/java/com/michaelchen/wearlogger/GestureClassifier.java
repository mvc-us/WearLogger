package com.michaelchen.wearlogger;

import com.dtw.FastDTW;
import com.timeseries.TimeSeries;
import com.util.DistanceFunction;
import com.util.DistanceFunctionFactory;

import java.io.File;

/**
 * Created by michael on 6/7/15.
 */
public abstract class GestureClassifier {
    protected TimeSeries[] trainingSet;
    protected boolean[] positive; // true if is actual wanted gesture
    private static final int[] COL_TO_INCLUDE = {0,1,2,3};
    public static final int RADIUS = 20;

    public GestureClassifier(File[] trainingSetFiles, boolean[] positive) {
        trainingSet = new TimeSeries[trainingSetFiles.length];
        for(int i = 0; i < trainingSetFiles.length; i++) {
            File file = trainingSetFiles[i];
            TimeSeries t = new TimeSeries(file, COL_TO_INCLUDE, true, false, ',');
            trainingSet[i] = t;
        }
        this.positive = new boolean[positive.length];
        System.arraycopy(positive, 0, this.positive, 0, positive.length);
        initialize();
    }

    abstract void initialize();

    public boolean classify(File file) {
        TimeSeries t = new TimeSeries(file, COL_TO_INCLUDE, true, false, ',');
        double[] distances = new double[trainingSet.length];
        final DistanceFunction distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance");
        for (int i = 0; i < trainingSet.length; i++) {
            distances[i] = FastDTW.getWarpDistBetween(t, trainingSet[i], RADIUS, distFn);
        }
        return runClassification(t, distances);
    }

    public abstract boolean runClassification(TimeSeries toEval, final double[] distances);
}
