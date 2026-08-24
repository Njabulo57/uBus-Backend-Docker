package org.tracker.ubus.ubus.Components.Reports.Mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusActivityStatus;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Entity.BusOperationalHistory;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Enum.MaintenanceIssue;
import org.tracker.ubus.ubus.Components.Reports.DTO.Response.Bus.BusFleetWrapper;
import org.tracker.ubus.ubus.Components.Reports.DTO.Response.Bus.BusOperationalResponse;
import org.tracker.ubus.ubus.Components.Reports.DTO.Response.PeekHourResponse;
import org.tracker.ubus.ubus.Components.Reports.DTO.Response.PeekResponseWrapper;
import org.tracker.ubus.ubus.Components.Reports.DTO.Response.MaxPeekHoursInsight;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportsMapper {


    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public PeekResponseWrapper toDTO(Collection<Trip> trips, double PEEK_HOUR_INDICATOR) {

        var startTime = System.currentTimeMillis();

        var hourlyAggregatedTrips = toHourMap(trips);
        var hourlyResponses = new ArrayList<PeekHourResponse>();
        var peakHours = new ArrayList<Integer>();
        var insights = new ArrayList<MaxPeekHoursInsight>();


        for (var hourEntry : hourlyAggregatedTrips.entrySet()) {
            var tripsByHour = hourEntry.getValue();
            int hour = hourEntry.getKey();

            var totalPassengersForHour = tripsByHour.stream()
                    .mapToInt(Trip::getTotalCount)
                    .sum();

            var totalCapacity = tripsByHour.stream()
                    .mapToInt(trip -> trip.getBusAssignment()
                            .getBus().getCapacity()
                    ).sum();

            double hourPercentage = totalCapacity > 0
                    ? (double) totalPassengersForHour / totalCapacity
                    : 0;

            var convertedPercent = (int) Math.round(hourPercentage * 100);
            var formattedHour = formatHour(hour);
            var hourResponse = new PeekHourResponse(formattedHour, convertedPercent);
            hourlyResponses.add(hourResponse);

            // Check if this hour qualifies as a PEAK hour (95% or above)
            if (hourPercentage >= PEEK_HOUR_INDICATOR) {
                peakHours.add(hour);

                // Build insight for this peak hour
                if (!tripsByHour.isEmpty()) {
                    var firstTrip = tripsByHour.getFirst();
                    var scheduleLeg = firstTrip.getScheduleLegBusAssignment();
                    if (scheduleLeg != null) {
                        var leg = scheduleLeg.getScheduleLeg();
                        insights.add(MaxPeekHoursInsight.builder()
                                .from(leg.getFromDestination())
                                .to(leg.getToDestination())
                                .message(String.format("Peak hour %s: %.0f%% load",
                                        formattedHour, hourPercentage * 100))
                                .build());
                    }
                }
            }

        }

        var endTime = System.currentTimeMillis();
        var timeTakenInSec = (startTime - endTime) / 1000.0;

        log.info("Time taken to process peek hours: {} seconds", timeTakenInSec);
        return new PeekResponseWrapper(
                hourlyResponses,
                peakHours,
                insights
        );
    }

    private Map<Integer, List<Trip>> toHourMap(Collection<Trip> trips) {

        return trips.stream()
                .collect(Collectors.groupingBy(trip -> {
                    var departureTime = trip.getDepartureTime();
                    return departureTime.getHour();
                }));
    }

    private String formatHour(int hour) {
        return String.format("%02d:00", hour);
    }


    public BusFleetWrapper toBusFleetWrapper(int totalBuses, int availableBuses, int inCriticalCondition,
                                             MaintenanceIssue mostCommonIssue,
                                             List<MaintenanceIssue> top3Issues,
                                             List<BusOperationalResponse> operationalStatus) {

        var mostCommonIssueLabel = mostCommonIssue != null ? mostCommonIssue.getLabel() : "None";
        var stringMaintenanceTypes = top3Issues.stream()
                .map(MaintenanceIssue::getLabel)
                .toList();

        return BusFleetWrapper.builder()
                .totalBuses(totalBuses)
                .availableBuses(availableBuses)
                .top3Issues(stringMaintenanceTypes)
                .mostCommonIssue(mostCommonIssueLabel)
                .inCriticalCondition(inCriticalCondition)
                .operationalStatus(operationalStatus)
                .build();
    }


    public BusOperationalResponse toBusOperationalResponse(Bus busFromHistory, int openIssues,
                                                           int openCriticalIssues, MaintenanceIssue topIssue,
                                                           LocalDate lastReported, String action,
                                                           int healthScore, Bus currentBus) {

        var topIssueLabel = topIssue != null ? topIssue.getLabel() : "None";
        var busName = busFromHistory.getName();
        var busType = busFromHistory.getType();
        var currentOperationalStatus = currentBus.getOperationalStatus();

        var formattedDate = lastReported.format(formatter);
        return BusOperationalResponse.builder()
                .busName(busName)
                .busType(busType.getDescription())
                .openIssues(openIssues)
                .openCriticalIssues(openCriticalIssues)
                .topIssue(topIssueLabel)
                .lastReported(formattedDate)
                .operationalStatus(currentOperationalStatus.getLabel())
                .action(action)
                .healthScore(healthScore)
                .build();
    }




}
