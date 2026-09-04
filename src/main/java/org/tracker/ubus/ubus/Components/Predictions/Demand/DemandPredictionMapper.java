package org.tracker.ubus.ubus.Components.Predictions.Demand;

import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.TripTraffic.Entity.Traffic;
import org.tracker.ubus.ubus.MLAlgorithms.RandomForstAlgorithm.Features.DemandFeature;

import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class DemandPredictionMapper {

    public List<DemandFeature> toFeatures(List<Trip> demandFeatures, List<Traffic> trafficFeatures) {

        return demandFeatures.stream()
                .map(trip -> {

                    var origin = getOriginFromTrip(trip);
                    var dayOfWeek = getDayOfWeek(trip);
                    var hourOfDay = getHourOfDay(trip);
                    var delayInMinutes = getDelayInMinutes(trip);

                    DemandFeature feature = DemandFeature.builder()
                            .semesterWeek(0)
                            .campus(origin)
                            .hasAccident(false)
                            .hasRoadworks(false)
                            .delayInMinutes(delayInMinutes)
                            .passengerCount(trip.countPassengers())
                            .route(trip.getRoute())
                            .hourOfDay(hourOfDay)
                            .dayOfWeek(dayOfWeek)
                            .build();
                    return feature;
                })
                .toList();
    }

    private int getHourOfDay(Trip trip) {
        return trip.getDepartureTime()
                .getHour();
    }

    public DayOfWeek getDayOfWeek(Trip trip) {
        return trip.getDepartureTime()
                .toLocalDate()
                .getDayOfWeek();
    }

    private Destination getOriginFromTrip(Trip trip) {
        return trip.getScheduleLegBusAssignment()
                .getScheduleLeg()
                .getToDestination();
    }

    private int getDelayInMinutes(Trip trip) {

        var actualArrivalTime = trip.getActualArrivalTime();
        var expectedArrivalTime = trip.getExpectedArrivalTime();

        if(actualArrivalTime == null || expectedArrivalTime == null) return 0;

        return (int) ChronoUnit.MINUTES.between(actualArrivalTime, expectedArrivalTime);
    }


}
