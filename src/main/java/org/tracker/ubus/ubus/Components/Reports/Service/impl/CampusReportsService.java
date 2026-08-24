package org.tracker.ubus.ubus.Components.Reports.Service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.tracker.ubus.ubus.Components.Reports.Abstract.AbstractReportService;
import org.tracker.ubus.ubus.Components.Reports.DTO.Response.Campus.CampusAnalytics;
import org.tracker.ubus.ubus.Components.Reports.DTO.Response.Campus.CampusAnalyticsWrapper;
import org.tracker.ubus.ubus.Components.Reports.Enum.ReportsTime;
import org.tracker.ubus.ubus.Components.Reports.Service.interfece.ICampusService;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleLegBusAssignment;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Repository.ScheduleLegAssignmentRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampusReportsService extends AbstractReportService implements ICampusService {

    private static final int MAX_PEAK_WINDOWS = 6;
    private static final double PEEK_HOURS_THRESHOLD = 0.85;
    private static final int PEEK_TIME_MINUTES_THRESHOLD = 20;


    private final TripRepository tripRepository;
    private final ScheduleLegAssignmentRepository scheduleLegAssignmentRepository;


    @Override
    public CampusAnalyticsWrapper getCampusBusyness(ReportsTime reportsTime) {

        var currentTime = System.currentTimeMillis();

        var endTime = LocalDateTime.now();
        var startTime = this.getTimeRangeByReportingFilter(reportsTime);
        var startOfDayOfStartTime = startTime.toLocalDate()
                .atStartOfDay();

        var tripStatus = TripStatus.COMPLETE;
        var completedTrips = this.tripRepository.findCompletedTripsForAllDestinations(startOfDayOfStartTime,
                endTime,
                tripStatus);


        var tripsGroupedByDestination = completedTrips.stream()
                .collect(Collectors.groupingBy(trip -> trip.getScheduleLegBusAssignment()
                        .getScheduleLeg()
                        .getFromDestination())
                );

        List<CampusAnalytics> campusAnalyticsList = new ArrayList<>();

        for (var entry : tripsGroupedByDestination.entrySet()) {

            Destination campus = entry.getKey();
            var trips = entry.getValue();

            // Average utilization for the campus
            var averageUtilizationForDestination = calculateAverageUtilization(trips);

            // getting all peak trips above the threshold
            var peakTrips = this.getPeakTrips(trips);
            var timesExceedingThreshold = peakTrips.size();

            // Getting the top 3 peak trips sorted by utilization descending
            var topPeakTrips = peakTrips.stream()
                    .sorted((t1, t2) -> Double.compare(
                            calculatePeakUtilization(t2),
                            calculatePeakUtilization(t1)
                    ))
                    .limit(MAX_PEAK_WINDOWS)
                    .toList();

            // Build peak time windows
            List<CampusAnalytics.PeakTimeWindow> peakTimeWindows = new ArrayList<>();

            for (Trip trip : topPeakTrips) {
                LocalTime startPeakTime = getPeakTime(trip, true);
                LocalTime endPeakTime = getPeakTime(trip, false);
                double utilization = calculatePeakUtilization(trip);

                // Count trips within this window.
                int tripCountInWindow = (int) peakTrips.stream()
                        .filter(p -> {
                            LocalTime departureTime = p.getDepartureTime().toLocalTime();
                            return !departureTime.isBefore(startPeakTime) &&
                                    !departureTime.isAfter(endPeakTime);
                        })
                        .count();

                var utilizationForPeak = Math.round(utilization * 10) / 10.0;
                var build = CampusAnalytics.PeakTimeWindow.builder()
                        .startTime(startPeakTime)
                        .endTime(endPeakTime)
                        .utilization(utilizationForPeak)
                        .tripCount(tripCountInWindow)
                        .build();
                peakTimeWindows.add(build);
            }

            // Highest peak utilization (from top peak trip)
            double peakUtilization = topPeakTrips.isEmpty() ? 0.0 :
                    calculatePeakUtilization(topPeakTrips.getFirst());

            // Fleet info
            var totalAvailableBusesForDestination = this.getAvailableBusesForDestination(campus);

            // Buses needed to get below threshold
            int busesNeededToGetBelowThreshold = peakUtilization >= (PEEK_HOURS_THRESHOLD * 100)
                    ? (int) Math.ceil(totalAvailableBusesForDestination * (peakUtilization / (PEEK_HOURS_THRESHOLD * 100)))
                    : totalAvailableBusesForDestination;

            int busShortage = Math.max(0, busesNeededToGetBelowThreshold - totalAvailableBusesForDestination);
            int excessBuses = Math.max(0, totalAvailableBusesForDestination - busesNeededToGetBelowThreshold);

            // Status
            String status;
            if (peakUtilization >= (PEEK_HOURS_THRESHOLD * 100) && busShortage > 0)
                status = "OVERLOADED";
            else if (averageUtilizationForDestination < 60 && excessBuses > 0)
                status = "UNDERUTILIZED";
            else
                status = "OPTIMAL";

            var averageUtilization = Math.round(averageUtilizationForDestination * 10) / 10.0;
            CampusAnalytics campusAnalytics = CampusAnalytics.builder()
                    .campus(campus.name())
                    .averageUtilization(averageUtilization)
                    .peakTimeWindows(peakTimeWindows)
                    .timesExceededThreshold(timesExceedingThreshold)
                    .totalTrips(trips.size())
                    .totalBusesAssigned(totalAvailableBusesForDestination)
                    .busesNeeded(busesNeededToGetBelowThreshold)
                    .busShortage(busShortage)
                    .excessBuses(excessBuses)
                    .status(status)
                    .build();

            campusAnalyticsList.add(campusAnalytics);
        }

        campusAnalyticsList.sort((a, b) -> {
            double bPeak = b.getPeakTimeWindows().isEmpty() ? 0 : b.getPeakTimeWindows().getFirst()
                                                                  .getUtilization();
            double aPeak = a.getPeakTimeWindows().isEmpty() ? 0 : a.getPeakTimeWindows().getFirst()

                                                                  .getUtilization();
            return Double.compare(bPeak, aPeak);
        });

        var endProcessingTime = System.currentTimeMillis();
        var totalTimeTaken = (endProcessingTime - currentTime) / 1000.0;

        log.info("Time taken to process campus analytics: {} seconds", totalTimeTaken);

        return CampusAnalyticsWrapper.builder()
                .campuses(campusAnalyticsList)
                .build();
    }


    private double calculateAverageUtilization(List<Trip> trips) {

        return trips.stream()
                .mapToDouble(trip -> {

                    var busAssignment = trip.getBusAssignment();
                    var bus = busAssignment.getBus();

                    return (double) trip.getTotalCount() / bus.getCapacity();
                })
                .average()
                .orElse(0.0) * 100;
    }

    private int getAvailableBusesForDestination(Destination origin) {
        var assignments = this.scheduleLegAssignmentRepository.findAvailableAssignedBusesForDestination(origin);

        return (int) assignments.stream()
                .map(ScheduleLegBusAssignment::getBus)
                .distinct()
                .count();
    }

    private LocalTime getPeakTime(Trip trip, boolean isPeakStart) {

        var exactTime = trip.getDepartureTime()
                .toLocalTime();

        if (isPeakStart)
            return exactTime.minusMinutes(PEEK_TIME_MINUTES_THRESHOLD);
        return exactTime.plusMinutes(PEEK_TIME_MINUTES_THRESHOLD);
    }

    private double calculatePeakUtilization(Trip trip) {
        var busAssignment = trip.getBusAssignment();
        var bus = busAssignment.getBus();
        return ((double) trip.getTotalCount() / bus.getCapacity()) * 100;
    }

    private List<Trip> getPeakTrips(List<Trip> trips) {

        return trips.stream()
                .filter(trip -> {

                    var busAssignment = trip.getBusAssignment();
                    var bus = busAssignment.getBus();

                    var utilization = (double) trip.getTotalCount() / bus.getCapacity();
                    return utilization > PEEK_HOURS_THRESHOLD;
                })
                .toList();
    }
}