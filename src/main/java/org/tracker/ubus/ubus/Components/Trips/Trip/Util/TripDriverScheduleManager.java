package org.tracker.ubus.ubus.Components.Trips.Trip.Util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleLeg;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleLegBusAssignment;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Repository.ScheduleLegAssignmentRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;


@Component
@RequiredArgsConstructor
public class TripDriverScheduleManager {

    private final ScheduleLegAssignmentRepository legAssignmentRepository;
    private final TripRepository tripRepository;

    public ScheduleLegBusAssignment getDriverCurrentScheduleOrThrow(BusAssignment busAssignment) {

        var bus = busAssignment.getBus();
        var today = LocalDate.now();
        var dayOfWeek = today.getDayOfWeek();

        // Get all schedule legs for today
        var scheduleLegs = this.legAssignmentRepository.findDriverScheduleTripsForToday(bus, today, dayOfWeek);

        // Get all trips done by this bus assignment for today
        var completeStatus = TripStatus.COMPLETE;
        var trips = this.tripRepository.findAllTripsByBusAssignmentForToday(bus, today,
                completeStatus);


        // Find the first schedule leg that doesn't have a completed trip yet
        return scheduleLegs.stream()
                .filter(sla -> {
                    boolean hasCompletedTrip = trips.stream()
                            .anyMatch(trip -> trip.getScheduleLegBusAssignment().getId()
                                    .equals(sla.getId())
                                    && trip.getStatus() == TripStatus.COMPLETE);
                    return !hasCompletedTrip;
                })
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No more schedule legs available for today"));
    }


    public void updateDriverCurrentSchedule(ScheduleLegBusAssignment scheduleLegBusAssignment) {
        this.legAssignmentRepository.save(scheduleLegBusAssignment);
    }
}