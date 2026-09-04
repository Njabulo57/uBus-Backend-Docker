package org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Forest.DemandRandomForest;





import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Features.DemandFeature;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Forest.Abstract.RandomForest;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Prediction.Demand.DemandPredictionStrategy;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Split.Demand.DemandSplitStrategy;

public class DemandRandomForestTree extends RandomForest<DemandFeature> {



    public DemandRandomForestTree() {
         super(new DemandPredictionStrategy(), new DemandSplitStrategy(new DemandPredictionStrategy()),
            0.75, 100,
                 4, 20);
    }

    public DemandRandomForestTree(int maxDepth, int numberOfTrees, int numberOfFeatures, double sampleSizePerTree) {
        super(new DemandPredictionStrategy(), new DemandSplitStrategy(new DemandPredictionStrategy()),
                sampleSizePerTree, numberOfTrees,
                numberOfFeatures, maxDepth);

    }


}
