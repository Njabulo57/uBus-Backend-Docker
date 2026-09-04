package org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Split.Demand;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Features.AbstractFeature;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Features.DemandFeature;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Prediction.Abstract.PredictionStrategy;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Split.Abstract.SplitStrategy;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.SplitResult;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.SplittingFeature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination.*;


@Slf4j
@Component
@RequiredArgsConstructor
public class DemandSplitStrategy implements SplitStrategy<DemandFeature> {


    private static final int MIN_SPLIT_SIZE = 5;

    // The prediction strategy is used to calculate variance and gain
    private final PredictionStrategy<DemandFeature> predictionStrategy;


    @Override
    public SplitResult<DemandFeature> findBestSplit(Collection<DemandFeature> data) {

        // If we don't have enough data, we can't find a good split
        if (data == null || data.size() < MIN_SPLIT_SIZE * 2) {

            System.err.println("not enough data to split");
            return null;
        }

        String best= "";
        double bestGain = Double.MIN_VALUE;
        SplitResult<DemandFeature> bestSplit = null;

        // we split it by the morning peak hours (6-9am)
        // This is a good feature because more students travel during peak hours
        SplitResult<DemandFeature> morningPeakSplit = splitByMorningPeak(data);
        if (morningPeakSplit != null && morningPeakSplit.gain() > bestGain) {
            bestGain = morningPeakSplit.gain();
            best = "morningPeak";
            bestSplit = morningPeakSplit;
        }

        // we split it by the afternoon peak hours (3-6pm)
        // this is a good feature because more students travel during peak hours
        SplitResult<DemandFeature> afternoonPeakSplit = splitByAfternoonPeak(data);
        if (afternoonPeakSplit != null && afternoonPeakSplit.gain() > bestGain) {
            bestGain = afternoonPeakSplit.gain();
            best = "afternoonPeak";
            bestSplit = afternoonPeakSplit;
        }

        // split it by weekday vs weekend
        // this is a good feature because more students travel during weekdays
        SplitResult<DemandFeature> weekdaySplit = splitByWeekday(data);
        if (weekdaySplit != null && weekdaySplit.gain() > bestGain) {
            bestGain = weekdaySplit.gain();
            best = "weekday";
            bestSplit = weekdaySplit;
        }



        SplitResult<DemandFeature> apkSplit = splitByCampus(data, APK);
        if (apkSplit != null && apkSplit.gain() > bestGain) {
            bestGain = apkSplit.gain();
            best = "APK";
            bestSplit = apkSplit;
        }

        SplitResult<DemandFeature> apbSplit = splitByCampus(data, APB);
        if (apbSplit != null && apbSplit.gain() > bestGain) {
            bestGain = apbSplit.gain();
            best = "APB";
            bestSplit = apbSplit;
        }

        SplitResult<DemandFeature> dfcSplit = splitByCampus(data, DFC);
        if (dfcSplit != null && dfcSplit.gain() > bestGain) {
            bestGain = dfcSplit.gain();
            best = "DFC";
            bestSplit = dfcSplit;
        }

        SplitResult<DemandFeature> swcSplit = splitByCampus(data, SWC);
        if (swcSplit != null && swcSplit.gain() > bestGain) {
            bestGain = swcSplit.gain();
            best = "SWC";
            bestSplit = swcSplit;
        }

        SplitResult<DemandFeature> jbsSplit = splitByCampus(data, JBS);
        if (jbsSplit != null && jbsSplit.gain() > bestGain) {
            bestGain = jbsSplit.gain();
            best = "JBS";
            bestSplit = jbsSplit;
        }

        // Return the best split we found
        // If bestSplit is null, no good split was found
        return bestSplit;
    }

    /**
     * Split data by morning peak
     *
     * LEFT = morning peak hours (6-9am)
     * RIGHT = all other hours
     */
    private SplitResult<DemandFeature> splitByMorningPeak(Collection<DemandFeature> data) {
        // Create the predicate that tests if a data point is in morning peak
        // This is the condition that will be used at the node
        Predicate<DemandFeature> isMorningPeak = AbstractFeature::isMorningPeakHour;

        // Split the data using the predicate
        var featureThreshHold = SplittingFeature.MORNING_PEAK;
        return createSplit(data, featureThreshHold, isMorningPeak);
    }


    /**
     * Split data by afternoon peak (3-6pm)
     */
    private SplitResult<DemandFeature> splitByAfternoonPeak(Collection<DemandFeature> data) {
        Predicate<DemandFeature> isAfternoonPeak = AbstractFeature::isAfternoonPeakHour;
        var featureThreshHold = SplittingFeature.AFTERNOON_PEAK;
        return createSplit(data, featureThreshHold, isAfternoonPeak);
    }


    /**
     * Split data by weekday vs weekend
     */
    private SplitResult<DemandFeature> splitByWeekday(Collection<DemandFeature> data) {
        Predicate<DemandFeature> isWeekday = d -> !d.isWeekend();
        var featureThreshHold = SplittingFeature.WEEKDAY;
        return createSplit(data, featureThreshHold,isWeekday);
    }


    private SplitResult<DemandFeature> splitByCampus(Collection<DemandFeature> data, Destination campus) {
        Predicate<DemandFeature> isCampus = d -> campus.equals(d.getCampus());
        var featureThreshold = SplittingFeature.CAMPUS;
        return createSplit(data, featureThreshold, isCampus);
    }

    private SplitResult<DemandFeature> createSplit(
            Collection<DemandFeature> data,
            SplittingFeature featureThreshold,
            Predicate<DemandFeature> predicate) {

        //Split the data using the predicate
        List<DemandFeature> leftData = new ArrayList<>();
        List<DemandFeature> rightData = new ArrayList<>();

        for (var item : data)
            if (predicate.test(item))
                leftData.add(item);  // Predicate is TRUE → go LEFT
            else
                rightData.add(item); // Predicate is FALSE → go RIGHT


        // Check if the split is valid
        // We need at least MIN_SPLIT_SIZE on both sides
        if (leftData.size() < MIN_SPLIT_SIZE || rightData.size() < MIN_SPLIT_SIZE) {
            log.trace("Split is not valid: {} vs {}", leftData.size(), rightData.size());
            return null; // Not enough data on one side - skip this split
        }


        double gain = calculateGain(data, leftData, rightData);

        // If gain is not positive, this split doesn't help
        if (gain <= 0) {
            log.trace("Split gain is not positive: {}", gain);
            return null;
        }

        //Creating and returning the SplitResult
        return new SplitResult<>(gain, null, leftData, rightData,
                featureThreshold, predicate);
    }

    /**
     * Calculate the gain of a split.
     * Gain = Reduction in variance after splitting.
     * Step 1: Calculate variance of all data before splitting
     * Step 2: Calculate weighted average variance after splitting
     * Step 3: Gain = varianceBefore - varianceAfter
     * Higher gain means the split makes the data more "pure"
     * (values in each group are more similar)
     *
     * @param allData All data before splitting
     * @param leftData Data that goes left
     * @param rightData Data that goes right
     * @return The gain (reduction in variance)
     */
    private double calculateGain(
            Collection<DemandFeature> allData,
            Collection<DemandFeature> leftData,
            Collection<DemandFeature> rightData) {

        // Calculate variance before splitting
        // This measures how spread out the passenger counts are
        double varianceBefore = predictionStrategy.calculateVariance(allData);


        //  Calculate variance after splitting
        // Each group should have lower variance (more similar)
        double varianceLeft = predictionStrategy.calculateVariance(leftData);
        double varianceRight = predictionStrategy.calculateVariance(rightData);

        // Calculate weighted average variance after splitting
        // Weight each group by its size
        double totalSize = allData.size();
        double weightLeft = leftData.size() / totalSize;
        double weightRight = rightData.size() / totalSize;

        double varianceAfter = (weightLeft * varianceLeft) + (weightRight * varianceRight);
        return varianceBefore - varianceAfter;
    }
}