package org.tracker.ubus.ubus.Components.Predictions.Demand;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;

import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Features.DemandFeature;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Forest.DemandRandomForest.DemandRandomForestTree;
import org.tracker.ubus.ubus.Util.MathUtil;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


@Slf4j
@Component
@RequiredArgsConstructor
public class DemandPredictions {


    private final Executor executor = Executors.newSingleThreadExecutor();
    private final TripRepository tripRepository;
    private final DemandPredictionMapper demandPredictionMapper;
    private DemandRandomForestTree demandRandomForestTree;


    @PostConstruct
    protected void init() {
        this.demandRandomForestTree = new DemandRandomForestTree(6, 80,
                4,0.9);

            executor.execute(this::predict);
    }


    private void predict()  {
        log.trace("Training demand predictions");

        var date = getDateFromNow(10);
        var currentDate = LocalDateTime.now();

        var trips = this.tripRepository.findCompletedTripsForAllDestinations(date, currentDate,
                TripStatus.COMPLETE);

        System.err.println("Trips found for demand prediction: " + trips.size());
        var demandFeatures = this.demandPredictionMapper.toFeatures(trips,
                new ArrayList<>());

        var start = System.currentTimeMillis();
        demandRandomForestTree.train(demandFeatures);
        var end = System.currentTimeMillis();


        var timeInSeconds = (end - start) / 1000.0;
        System.err.println("Time taken to train demand predictions: " + timeInSeconds + " seconds");
        log.trace("Demand predictions trained");

        DemandFeature p = DemandFeature.builder()
                .hourOfDay(8)
                .campus(Destination.APK)
                .dayOfWeek(DayOfWeek.MONDAY)      // Monday
                .passengerCount(0) // Not needed for prediction
                .build();

        var prediction = this.demandRandomForestTree.predict(p);
        System.err.println("predcition for  " + p + " is " + MathUtil.round(prediction, 2) + "passengers");


        DemandFeature p2 = DemandFeature.builder()
                .hourOfDay(8)
                .campus(Destination.JBS)
                .dayOfWeek(DayOfWeek.SATURDAY)      // Monday
                .passengerCount(0) // Not needed for prediction
                .build();
        var prediction2 = this.demandRandomForestTree.predict(p2);
        System.err.println("predcition for  " + p2 + " is " + MathUtil.round(prediction2, 2) + "passengers");
    }

    private LocalDateTime getDateFromNow(int months) {
        return LocalDateTime.now()
                .minusMonths(months);
    }


}
