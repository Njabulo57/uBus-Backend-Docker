package org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Prediction.Traffic;


import lombok.extern.slf4j.Slf4j;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Features.TrafficFeature;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Prediction.Abstract.PredictionStrategy;

import java.util.Collection;


/**
 * Prediction strategy for traffic delay.
 *
 * This class knows how to:
 * 1. Calculate the average delay (for leaf nodes)
 * 2. Check if all delays are the same
 * 3. Extract the delay from a data point
 */
@Slf4j
public class TrafficPredictionStrategy implements PredictionStrategy<TrafficFeature> {



    @Override
    public double calculateAverageValue(Collection<TrafficFeature> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }

        double sum = data.stream()
                .mapToDouble(TrafficFeature::getDelayInMinutes)
                .sum();

        return sum / data.size();
    }

    @Override
    public boolean isAllSame(Collection<TrafficFeature> data) {
        if (data == null || data.size() <= 1)
            return true;


        var isAllSame = data.stream()
                .anyMatch(d -> d.getDelayInMinutes() != data.iterator().next()
                        .getDelayInMinutes()
                );

        log.info("Is all traffic delays the same? {}", isAllSame);
        return isAllSame;
    }


    @Override
    public double getValue(TrafficFeature item) {
        return item.getDelayInMinutes();
    }


}