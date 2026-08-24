package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.Schedule;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleLeg;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {


    @Query("""
        SELECT DISTINCT s FROM Schedule s
        LEFT JOIN FETCH s.scheduleLegs legs
        LEFT JOIN FETCH legs.busesAssigned ba
        LEFT JOIN FETCH ba.bus
        WHERE s.validFromDate <= :date AND s.validToDate >= :date
    """)
    List<Schedule> findAllActiveSchedulesForDate(@Param("date") LocalDate date);




    @Query("""
        SELECT s FROM Schedule s
        WHERE s.validFromDate <= :date AND s.validToDate >= :date
        AND s.route = :route
    """)
    Optional<Schedule> findActiveScheduleByRoute(
            @Param("route") Route route,
            @Param("date") LocalDate date
    );

    @Query("""
        SELECT s FROM Schedule s
        WHERE s.validFromDate <= :date AND s.validToDate >= :date
        AND s.route = :route
        AND NOT EXISTS (
            SELECT 1 FROM ScheduleDatesExcluded de
            WHERE de.schedule = s
            AND de.fromDate <= :date AND de.toDate >= :date
        )
    """)
    Optional<Schedule> findActiveScheduleByRouteExcludingDates(
            @Param("route") Route route,
            @Param("date") LocalDate date
    );

    @Query("""
        SELECT s FROM Schedule s
        WHERE s.validFromDate <= :date AND s.validToDate >= :date
    """)
    List<Schedule> findActiveSchedulesForDate(@Param("date") LocalDate date);

    @Query("""
        SELECT s FROM Schedule s
        WHERE s.validFromDate >= :fromDate AND s.validToDate <= :toDate
    """)
    List<Schedule> findSchedulesInDateRange(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    // ============================================
    // SCHEDULE LEG QUERIES
    // ============================================

    @Query("""
        SELECT sl FROM ScheduleLeg sl
        WHERE sl.schedule.id = :scheduleId
        AND sl.departureTime >= :startTime
        AND sl.arrivalTime <= :endTime
        ORDER BY sl.departureTime ASC
    """)
    List<ScheduleLeg> findScheduleLegsByScheduleAndTimeRange(
            @Param("scheduleId") UUID scheduleId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("""
        SELECT sl FROM ScheduleLeg sl
        WHERE sl.schedule.id = :scheduleId
        AND sl.departureTime >= :currentTime
        AND sl.arrivalTime <= :endTime
        AND EXISTS (
            SELECT 1 FROM ScheduleLegBusAssignment ba
            WHERE ba.scheduleLeg = sl
            AND ba.isCompleted = FALSE
        )
        ORDER BY sl.departureTime ASC
        LIMIT 1
    """)
    Optional<ScheduleLeg> findNextActiveScheduleLeg(
            @Param("scheduleId") UUID scheduleId,
            @Param("currentTime") LocalDateTime currentTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("""
        SELECT sl FROM ScheduleLeg sl
        WHERE sl.schedule.id = :scheduleId
        AND sl.departureTime >= :currentTime
        AND sl.arrivalTime <= :endTime
        ORDER BY sl.departureTime ASC
        LIMIT 2
    """)
    List<ScheduleLeg> findNextTwoScheduleLegs(
            @Param("scheduleId") UUID scheduleId,
            @Param("currentTime") LocalDateTime currentTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("""
        SELECT sl FROM ScheduleLeg sl
        WHERE sl.schedule.route = :route
        AND sl.departureTime >= :currentTime
        AND EXISTS (
            SELECT 1 FROM ScheduleLegBusAssignment ba
            WHERE ba.scheduleLeg = sl
            AND ba.isCompleted = FALSE
        )
        ORDER BY sl.departureTime ASC
        LIMIT 1
    """)
    Optional<ScheduleLeg> findNextActiveScheduleLegByRoute(
            @Param("route") Route route,
            @Param("currentTime") LocalDateTime currentTime
    );






    @Query(value = """
        SELECT DISTINCT s.* FROM schedule s
        JOIN schedule_leg sl ON sl.schedule_id = s.id
        JOIN schedule_leg_bus_assignment ba ON ba.schedule_leg_id = sl.id
        JOIN bus_assignment b ON b.id = ba.bus_assignment_id
        WHERE s.valid_from_date <= :date AND s.valid_to_date >= :date
        AND sl.departure_time >= :startTime AND sl.arrival_time <= :endTime
        AND ba.is_completed = FALSE
        ORDER BY sl.departure_time ASC
    """, nativeQuery = true)
    List<Schedule> findActiveSchedulesWithActiveBusesForTimeRange(
            @Param("date") LocalDate date,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("""
        SELECT sl FROM ScheduleLeg sl
        WHERE sl.schedule.route = :route
        AND sl.departureTime >= :currentTime
        AND EXISTS (
            SELECT 1 FROM ScheduleLegBusAssignment ba
            WHERE ba.scheduleLeg = sl
            AND ba.isCompleted = FALSE
        )
        ORDER BY sl.departureTime ASC
    """)
    List<ScheduleLeg> findAllActiveScheduleLegsByRoute(
            @Param("route") Route route,
            @Param("currentTime") LocalDateTime currentTime
    );

    @Query("""
        SELECT sl FROM ScheduleLeg sl
        WHERE sl.schedule.id = :scheduleId
        AND sl.departureTime >= :currentTime
        ORDER BY sl.departureTime ASC
    """)
    List<ScheduleLeg> findUpcomingLegsForSchedule(
            @Param("scheduleId") UUID scheduleId,
            @Param("currentTime") LocalDateTime currentTime
    );




    @Query("""
        SELECT sl FROM ScheduleLeg sl WHERE sl.id = :id
    """)
    Optional<ScheduleLeg> findLegById(@Param("id") UUID id);

    @Query("""
        SELECT COUNT(sl) FROM ScheduleLeg sl
        WHERE sl.schedule.id = :scheduleId
        AND sl.departureTime >= :startTime
        AND sl.arrivalTime <= :endTime
    """)
    long countActiveLegsForSchedule(
            @Param("scheduleId") UUID scheduleId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("""
        SELECT CASE WHEN COUNT(sl) > 0 THEN TRUE ELSE FALSE END
        FROM ScheduleLeg sl
        WHERE sl.id = :legId
        AND sl.departureTime <= :currentTime
        AND sl.arrivalTime >= :currentTime
    """)
    boolean isLegActiveNow(
            @Param("legId") UUID legId,
            @Param("currentTime") LocalDateTime currentTime
    );



    default Schedule findByIdOrThrow(UUID id) {
        return this.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule Not Found."));
    }

    default ScheduleLeg findLegByIdOrThrow(UUID id) {
        return this.findLegById(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule Leg Not Found."));
    }

}