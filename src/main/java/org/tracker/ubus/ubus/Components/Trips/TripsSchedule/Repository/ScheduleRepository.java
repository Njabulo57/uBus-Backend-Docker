package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.Schedule;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {



    @Query("""
        SELECT s FROM Schedule s
        WHERE s.serviceDate = :date
        AND s.departureTime > :currentTime
        ORDER BY s.departureTime ASC
    """)
    List<Schedule> findUpcomingSchedulesForDate(
            @Param("date") LocalDate date,
            @Param("currentTime") LocalTime currentTime
    );


    @Query("""
        SELECT s FROM Schedule s
        LEFT JOIN FETCH s.bus WHERE s.bus = :bus
        AND s.serviceDate = :serviceDate
        AND s.departureTime >= :startTime AND s.arrivalTime <= :endTime
        ORDER BY s.departureTime ASC
    """)
    List<Schedule> findByBusAndServiceDateAndTimeRange(
            @Param("bus") Bus bus,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("""
        SELECT s FROM Schedule s
        LEFT JOIN FETCH s.bus WHERE s.bus = :bus
        AND s.serviceDate = :serviceDate
        AND s.departureTime >= :startTime AND s.arrivalTime <= :endTime
        AND s.isCompleted = FALSE
        ORDER BY s.departureTime ASC
        LIMIT 1
    """)
    Optional<Schedule> findDriverCurrentScheduleTrip(
            @Param("bus") Bus bus,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("""
        SELECT s FROM Schedule s
        LEFT JOIN FETCH s.bus WHERE s.bus = :bus
        AND s.serviceDate = :serviceDate
        AND s.departureTime >= :startTime AND s.arrivalTime <= :endTime
        AND s.isCompleted = FALSE
        ORDER BY s.departureTime ASC
        LIMIT 2
    """)
    List<Schedule> findDriverNextScheduleTrip(
            @Param("bus") Bus bus,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    List<Schedule> findByBusAndServiceDate(Bus bus, LocalDate date);


    List<Schedule> findByServiceDate(LocalDate date);


    default Schedule findByIdOrThrow(UUID id) {
        return this.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule Not Found."));
    }
}