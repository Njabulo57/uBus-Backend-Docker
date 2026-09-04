package org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Prediction.Demand;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Features.DemandFeature;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Prediction.Abstract.PredictionStrategy;

import java.util.Collection;

@Slf4j
@Component
public class DemandPredictionStrategy implements PredictionStrategy<DemandFeature> {


    @Override
    public boolean isAllSame(Collection<DemandFeature> data) {
        if (data == null || data.size() <= 1) return true;

        // Get the first value as reference
        int firstValue = data.iterator()
                .next()
                .getPassengerCount();


        return data.stream()
                .allMatch(d -> d.getPassengerCount() == firstValue);
    }

    @Override
    public double calculateAverageValue(Collection<DemandFeature> data) {

        if(data == null || data.isEmpty()) return 0;

        var average = data.stream()
                .mapToDouble(DemandFeature::getPassengerCount)
                .average()
                .orElse(0.0);

        return average;
    }


    @Override
    public double getValue(DemandFeature data) {
        return data.getPassengerCount();
    }

}
