package org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Split.Traffic;



import lombok.RequiredArgsConstructor;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Features.AbstractFeature;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Features.TrafficFeature;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Prediction.Abstract.PredictionStrategy;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Split.Abstract.SplitStrategy;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.SplitResult;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.SplittingFeature;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;


@RequiredArgsConstructor
public class TrafficSplitStrategy implements SplitStrategy<TrafficFeature> {

    private static final int MIN_SPLIT_SIZE = 5;
    private final PredictionStrategy<TrafficFeature> predictionStrategy;


    @Override
    public SplitResult<TrafficFeature> findBestSplit(Collection<TrafficFeature> data) {
        if (data == null || data.size() < MIN_SPLIT_SIZE * 2) {
            return null;
        }

        double bestGain = -1;
        SplitResult<TrafficFeature> bestSplit = null;

        // Try splitting by peak hour
        SplitResult<TrafficFeature> peakSplit = splitByPeakHour(data);
        if (peakSplit != null && peakSplit.gain() > bestGain) {
            bestGain = peakSplit.gain();
            bestSplit = peakSplit;
        }

        // Try splitting by speed (slow traffic)
        SplitResult<TrafficFeature> slowSplit = splitBySpeed(data);
        if (slowSplit != null && slowSplit.gain() > bestGain) {
            bestGain = slowSplit.gain();
            bestSplit = slowSplit;
        }

        // Try splitting by accident
        SplitResult<TrafficFeature> accidentSplit = splitByAccident(data);
        if (accidentSplit != null && accidentSplit.gain() > bestGain) {
            bestGain = accidentSplit.gain();
            bestSplit = accidentSplit;
        }

//        // Try splitting by roadworks
//        SplitResult<TrafficFeature> roadworksSplit = splitByRoadworks(data);
//        if (roadworksSplit != null && roadworksSplit.gain() > bestGain) {
//            bestGain = roadworksSplit.gain();
//            bestSplit = roadworksSplit;
//        }

//        // Try splitting by weather
//        SplitResult<TrafficFeature> weatherSplit = splitByWeather(data);
//        if (weatherSplit != null && weatherSplit.gain() > bestGain) {
//            bestGain = weatherSplit.gain();
//            bestSplit = weatherSplit;
//        }

        return bestSplit;
    }

    private SplitResult<TrafficFeature> splitByPeakHour(Collection<TrafficFeature> data) {
        Predicate<TrafficFeature> isPeakHour = AbstractFeature::isPeakHour;
        var peakSplitter = SplittingFeature.PEAK_HOUR;
        return createSplit(data, peakSplitter, isPeakHour);
    }

    private SplitResult<TrafficFeature> splitBySpeed(Collection<TrafficFeature> data) {
        // Split by speed below 20 km/h (traffic jam condition)
        Predicate<TrafficFeature> isSlow = d -> d.getCurrentSpeed() < 20;
        var splittingFeature = SplittingFeature.LOW_AVERAGE_SPEED;
        return createSplit(data, splittingFeature, isSlow);
    }

    private SplitResult<TrafficFeature> splitByAccident(Collection<TrafficFeature> data) {
        Predicate<TrafficFeature> hasAccident = TrafficFeature::isHasAccident;
        var accidentSplitter  = SplittingFeature.ACCIDENT_OCCURRED;
        return createSplit(data, accidentSplitter,  hasAccident);
    }

//    private SplitResult<TrafficFeature> splitByRoadworks(List<TrafficFeature> data) {
//        Predicate<TrafficFeature> hasRoadworks = d -> d.isHasRoadworks();
//        return createSplit(data, "hasRoadworks", "true", hasRoadworks);
//    }

//    private SplitResult<TrafficFeature> splitByWeather(List<TrafficFeature> data) {
//        Predicate<TrafficFeature> badWeather = d -> d.getWeatherScore() > 3;
//        return createSplit(data, "badWeather", "true", badWeather);
//    }

    private SplitResult<TrafficFeature> createSplit(
            Collection<TrafficFeature> data,
            SplittingFeature featureName,
            Predicate<TrafficFeature> predicate) {

        List<TrafficFeature> leftData = new ArrayList<>();
        List<TrafficFeature> rightData = new ArrayList<>();

        for (TrafficFeature item : data)
            if (predicate.test(item))
                leftData.add(item);
            else
                rightData.add(item);

        if (leftData.size() < MIN_SPLIT_SIZE || rightData.size() < MIN_SPLIT_SIZE)
            return null;


        double gain = calculateGain(data, leftData, rightData);

        if (gain <= 0) {
            return null;
        }

        return new SplitResult<>(gain, null, leftData, rightData, featureName, predicate);
    }

    private double calculateGain(
            Collection<TrafficFeature> allData,
            Collection<TrafficFeature> leftData,
            Collection<TrafficFeature> rightData) {

        double varianceBefore = predictionStrategy.calculateVariance(allData);

        double varianceLeft = predictionStrategy.calculateVariance(leftData);
        double varianceRight = predictionStrategy.calculateVariance(rightData);

        double totalSize = allData.size();
        double weightLeft = leftData.size() / totalSize;
        double weightRight = rightData.size() / totalSize;

        double varianceAfter = (weightLeft * varianceLeft) + (weightRight * varianceRight);

        return varianceBefore - varianceAfter;
    }
}