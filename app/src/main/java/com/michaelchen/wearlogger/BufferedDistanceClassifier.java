package com.michaelchen.wearlogger;

import com.dtw.FastDTW;
import com.timeseries.TimeSeries;

import java.io.File;

/**
 * Created by michael on 6/8/15.
 */
public class BufferedDistanceClassifier extends DistanceClassifier {
    public BufferedDistanceClassifier(File[] trainingSetFiles, boolean[] positive) {
        super(trainingSetFiles, positive);
    }

    @Override
    public boolean runClassification(TimeSeries toEval, double[] distances) {
        // all distances must be closer than maxInternalDistance
        for (TimeSeries t : correctTimeSeries) {
            double distance = FastDTW.getWarpDistBetween(toEval, t, RADIUS, distFn);
            if (distance < maxInternalDistance) return true;
        }
        return false;
    }
}
