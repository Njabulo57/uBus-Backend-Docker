package org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm;

import lombok.Builder;

import java.util.Collection;
import java.util.function.Predicate;

@Builder
public record SplitResult<T>(double gain, T value,
                             Collection<T> leftData, Collection<T> rightData, //data within the split
                             SplittingFeature threshold, Predicate<T> shouldGoLeft) { // threshold and predicate for evaluation


    public boolean shouldGoLeft(T data) {
        return shouldGoLeft.test(data);
    }


    public boolean isValid() {
        return !leftData.isEmpty() && !rightData.isEmpty();
    }

}
