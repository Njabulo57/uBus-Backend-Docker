package org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Forest.TrafficRandomForest;


import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Features.TrafficFeature;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Forest.Abstract.RandomForest;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Prediction.Traffic.TrafficPredictionStrategy;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Split.Traffic.TrafficSplitStrategy;

/**
 * Convenience class for predicting traffic delay.
 *
 * This just sets up the RandomForest with the correct strategies
 * for traffic prediction.
 */
public class TrafficRandomForest extends RandomForest<TrafficFeature> {

    public TrafficRandomForest() {
        super(new TrafficPredictionStrategy(), new TrafficSplitStrategy(new TrafficPredictionStrategy()),
                0.75, 100,
                4, 20);
    }

    public TrafficRandomForest(int numTrees, int maxFeatures, int maxDepth, double sampleSize) {
        super(new TrafficPredictionStrategy(), new TrafficSplitStrategy(new TrafficPredictionStrategy()),
                sampleSize, numTrees,
                maxFeatures, maxDepth);
    }
}