package org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Forest.Abstract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Tree.DecisionTree;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Tree.ITree;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Prediction.Abstract.PredictionStrategy;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Split.Abstract.SplitStrategy;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public abstract class RandomForest<T> implements IRandomForest<T> {

    private Collection<ITree<T>> trees;

    private int maxDepth;
    private int numberOfFeatures;
    private int numberOfTrees;
    private double sampleSizePerTree;
    private SplitStrategy<T> splitStrategy;
    private PredictionStrategy<T> predictionStrategy;


    private SecureRandom random = new SecureRandom();


    public RandomForest(PredictionStrategy<T> predictionStrategy, SplitStrategy<T> splitStrategy,
                        double sampleSizePerTree, int numberOfTrees, int numberOfFeatures, int maxDepth) {
        this.predictionStrategy = predictionStrategy;
        this.splitStrategy = splitStrategy;
        this.sampleSizePerTree = sampleSizePerTree;
        this.numberOfTrees = numberOfTrees;
        this.numberOfFeatures = numberOfFeatures;
        this.maxDepth = maxDepth;

        this.trees = new ArrayList<>(numberOfFeatures); //setting an arraylist with an internal max size of numberOfTrees

        log.info("Random Forest created with {} trees", numberOfTrees);
    }


    @Override
    public void train(List<T> trainingData) {
        log.info("Training forest with {} trees", trees.size());
        log.info("Size of training data {}" , trainingData.size());

        var loggingPoint = 10;
        var samplePercentage = trainingData.size() / sampleSizePerTree * 100;
        log.info("sample size {}%", samplePercentage);

        var now  = System.currentTimeMillis();
        for(int i = 0; i < this.numberOfTrees; i++) {


            var bootStrapSample = bootStrapSample(trainingData);

            //creating a decision tree with the bootstrapped sample that knows how to split and predict
            ITree<T> tree = new DecisionTree<>(splitStrategy, predictionStrategy);

           //building the tree with the bootstrapped sample
            tree.build(bootStrapSample);

            trees.add(tree); //add the tree to the forest
            logProgress(i, loggingPoint); //log progress eve
        }

        var endTime = System.currentTimeMillis();

        var trainingTime = (endTime - now) / 1000.0;
        log.info("Training took {} s", trainingTime);
        log.info("✅ Random Forest training complete! {} trees ready", trees.size());
    }


    @Override
    public double predict(T data) {

        if(!isTrained()) throw new IllegalStateException("Forest is not trained yet");

        var sum = 0.0;
        for(var tree : trees) {

            var value = tree.predict(data);
            sum += value;
        }
        return sum / trees.size();
    }

    @Override
    public boolean isTrained() {
        return !trees.isEmpty();
    }


    @Override
    public int getNumberOfTrees() {
        return trees.size();
    }


    private Collection<T> bootStrapSample(List<T> trainingData) {

        var size = (int) (trainingData.size() * sampleSizePerTree);
        if(size < 1) size = 1;
        var sample = new ArrayList<T>(size); //creating an arraylist with an internal max size of size

        var sizeOfTrainingData = trainingData.size();
        for(int i = 0; i < size; i++) {

            var randomIndex = random.nextInt(sizeOfTrainingData);
            var data = trainingData.get(randomIndex);
            sample.add(data);
        }
        return sample;
    }

    private void logProgress(int index, int logPoint) {
        if ((index + 1) % logPoint == 0) {
            log.info("🌳 Trained {}/{} trees", index + 1, numberOfTrees);
        }
    }
}
