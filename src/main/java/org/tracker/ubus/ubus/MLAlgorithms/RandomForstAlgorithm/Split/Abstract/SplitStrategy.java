package org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Split.Abstract;


import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.SplitResult;

import java.util.Collection;


public interface SplitStrategy<T> {


    SplitResult<T> findBestSplit(Collection<T> data);
}
