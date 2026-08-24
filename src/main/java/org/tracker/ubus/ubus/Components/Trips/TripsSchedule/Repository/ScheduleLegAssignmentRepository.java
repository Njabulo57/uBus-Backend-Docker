package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleLegBusAssignment;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduleLegAssignmentRepository extends JpaRepository<ScheduleLegBusAssignment, UUID> {



    @Query("""
    SELECT sla FROM ScheduleLegBusAssignment sla
        LEFT JOIN sla.scheduleLeg sl
        LEFT JOIN sl.schedule s
    WHERE sla.bus = :bus
    AND sla.isCompleted = FALSE AND s.validFromDate <= :today
    AND s.validToDate >= :today AND sl.dayOfWeek = :dayOfWeek
    ORDER BY sl.departureTime ASC
""")
    List<ScheduleLegBusAssignment> findDriverScheduleTripsForToday(
            @Param("bus") Bus bus,
            @Param("today") LocalDate today,
            @Param("dayOfWeek") DayOfWeek dayOfWeek
    );

    @Query("""
    SELECT sla FROM ScheduleLegBusAssignment sla
    LEFT JOIN FETCH sla.scheduleLeg sl
    LEFT JOIN FETCH sl.schedule s
    WHERE sla.bus = :bus
    AND sla.isCompleted = FALSE AND s.validFromDate <= :today
    AND s.validToDate >= :today
    ORDER BY sl.departureTime ASC
""")
    List<ScheduleLegBusAssignment> findDriverTripsForToday(
            @Param("bus") Bus bus,
            @Param("today") LocalDate today
    );


    @Query("""
        SELECT sla FROm ScheduleLegBusAssignment sla
        LEFT JOIN FETCH sla.bus b
        LEFT JOIN FETCH sla.scheduleLeg sl
        LEFT JOIN FETCH sl.schedule s
        WHERE sla.bus = :bus
        ORDER BY sl.departureTime ASC
        LIMIT 1
    """)
    Optional<ScheduleLegBusAssignment> findNextLegByBusAssignment(@Param("bus") Bus bus);

    @Query("""
        SELECT sla FROM ScheduleLegBusAssignment sla
        LEFT JOIN FETCH sla.scheduleLeg sl
        LEFT JOIN FETCH  sl.schedule s
        WHERE sla.bus = :bus
        AND sla.isCompleted = FALSE
        AND s.validFromDate <= :today
        AND s.validToDate >= :today
        ORDER BY sl.departureTime ASC
    LIMIT 1
""")
    Optional<ScheduleLegBusAssignment> findDriverCurrentScheduleTrip(
            @Param("bus") Bus bus,
            @Param("today") LocalDate today
    );


    @Query("""
        SELECT ba FROM ScheduleLegBusAssignment ba
        WHERE ba.scheduleLeg.id = :scheduleLegId
        AND ba.isCompleted = FALSE
    """)
    List<ScheduleLegBusAssignment> findActiveBusAssignmentsForLeg(
            @Param("scheduleLegId") UUID scheduleLegId
    );


    @Query("""
        SELECT slba FROM ScheduleLegBusAssignment slba
            LEFT JOIN FETCH slba.bus
            LEFT JOIN FETCH slba.scheduleLeg sl
        WHERE sl.fromDestination = :destination
    """)
    List<ScheduleLegBusAssignment> findAvailableAssignedBusesForDestination(@Param("destination") Destination destination);

    default ScheduleLegBusAssignment findNextLegByBusAssignmentOrThrow(Bus bus) {
        return this.findNextLegByBusAssignment(bus)
                .orElseThrow(() -> new IllegalArgumentException("ScheduleLegBusAssignment Not Found."));
    }
}
