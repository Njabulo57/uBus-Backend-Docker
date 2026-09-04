package org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Forest.Abstract;

import java.util.List;

public interface IRandomForest<T> {

    boolean isTrained();

    void train(List<T> data);

    double predict(T data);

    int getNumberOfTrees();
}
