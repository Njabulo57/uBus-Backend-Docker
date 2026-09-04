package org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Tree;

import java.util.Collection;

public interface ITree<T> {


    void build(Collection<T> trainingData);


    double predict(T data);
}
