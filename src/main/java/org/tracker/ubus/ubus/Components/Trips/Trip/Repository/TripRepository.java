package org.tracker.ubus.ubus.Components.Trips.Trip.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Exceptions.TripNotFoundException;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {

    @Query("""
    SELECT t FROM Trip t
    WHERE t.busAssignment.bus = :bus
    AND DATE(t.departureTime) = :today AND t.status =:status
    ORDER BY t.departureTime ASC
""")
    List<Trip> findAllTripsByBusAssignmentForToday(
            @Param("bus") Bus bus,
            @Param("today") LocalDate today,
            @Param("status") TripStatus status
    );

    @Query("""
        SELECT trip FROM Trip trip

            LEFT JOIN FETCH trip.busAssignment ba
            LEFT JOIN FETCH trip.scheduleLegBusAssignment slba
            LEFT JOIN FETCH slba.scheduleLeg sl
            LEFT JOIN FETCH ba.driver
            LEFT JOIN FETCH ba.driver
            LEFT JOIN FETCH ba.bus
        WHERE trip.id = :id
    """)
    Optional<Trip> findByIdFetch(@Param("id") UUID id);


    @Query("""
        SELECT t  FROM Trip t
        LEFT JOIN FETCH t.busAssignment ba
        LEFT JOIN FETCH ba.bus b
        WHERE t.status = 'IN_PROGRESS'
        AND b.id =: busId
    """)
    Optional<Trip> findActiveTripByBus(@Param("busId") UUID busId);

    Optional<Trip> findByStatusAndId(TripStatus status, UUID id);

    // Check if any trips exist between two dates
    boolean existsByDepartureTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(t) FROM Trip t WHERE t.departureTime BETWEEN :start AND :end")
    long countByDepartureTimeBetween(LocalDateTime start, LocalDateTime end);


    boolean existsByBusAssignmentAndDepartureTimeBetween(
            BusAssignment busAssignment,
            LocalDateTime start,
            LocalDateTime end
    );


    List<Trip> findByBusAssignmentAndStatus(BusAssignment busAssignment, TripStatus status);

    // Find trips by bus assignment and departure time between
    List<Trip> findByBusAssignmentAndDepartureTimeBetween(
            BusAssignment busAssignment,
            LocalDateTime start,
            LocalDateTime end
    );


    @Query("""
    SELECT t FROM Trip t
        LEFT JOIN FETCH t.busAssignment ba
        LEFT JOIN FETCH ba.bus b
        LEFT JOIN FETCH t.scheduleLegBusAssignment slba
        LEFT JOIN FETCH slba.scheduleLeg sl
    WHERE t.status = :status
    AND t.departureTime BETWEEN :startDate AND :endDate
    ORDER BY t.departureTime ASC
    """)
    List<Trip> findCompletedTripsForAllDestinations(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") TripStatus status
    );


    @Query("""
        SELECT t  FROM Trip t
        LEFT JOIN FETCH t.busAssignment ba
        LEFT JOIN FETCH ba.bus b
        WHERE t.status = 'IN_PROGRESS'
        AND ba = :busAssignment
    """)
    List<Trip> findByBusAssignment(@Param("busAssignment") BusAssignment busAssignment);


    @Query("""
       SELECT DISTINCT t FROM Trip t
       LEFT JOIN FETCH t.busAssignment ba
       JOIN FETCH ba.driver d
       WHERE ba = :busAssignment
       AND t.status IN :statuses
    """)
    List<Trip> findByBusAssignmentAndStatusIn(
            @Param("busAssignment") BusAssignment busAssignment,
            @Param("statuses") List<TripStatus> statuses
    );


    @Query("""
        SELECT t FROM Trip t
        LEFT JOIN FETCH t.busAssignment ba
        LEFT JOIN FETCH ba.driver
        WHERE t.status = 'IN_PROGRESS'
        AND t.route = : route
    """)
    List<Trip> findByRoute(@Param("route") Route route);

    @Query("""
        SELECT DISTINCT t FROM Trip t
        LEFT JOIN FETCH t.busAssignment ba
        lEFT JOIN FETCH ba.driver
        LEFT JOIN FETCH ba.bus b
        LEFT JOIN FETCH t.tripUsers tu
        WHERE t.status = :statusParam
    """)
    List<Trip> findByStatus(@Param("statusParam") TripStatus status);


    @Query("""
        SELECT DISTINCT t FROM Trip t
        LEFT JOIN FETCH t.busAssignment ba
        lEFT JOIN FETCH ba.driver
        LEFT JOIN FETCH ba.bus b
        LEFT JOIN FETCH t.tripUsers tu
        WHERE t.status = :statusParam AND  t.departureTime = :dateParam
    """)
    List<Trip> findByStatusAndDate(@Param("statusParam") TripStatus status,
                                   @Param("dateParam") LocalDateTime dateParam);

    @Query("""
        SELECT t
        FROM Trip t
        WHERE (CAST(:dateTime AS timestamp) IS NULL OR t.createdAt > :dateTime)
    """)
    List<Trip> findAllWithScheduleFetched(@Param("dateTime") LocalDateTime dateTime);

    List<Trip> findByDepartureTimeBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

    List<Trip> findByCreatedAtAfter(LocalDateTime createdAt);

    List<Trip> findByCreatedAtBefore(LocalDateTime createdAtBefore);


    @Query("""
    SELECT t FROM Trip t
    LEFT JOIN FETCH t.busAssignment ba
    LEFT JOIN FETCH ba.driver
    WHERE t.departureTime BETWEEN :startDate AND :endDate
""")
    List<Trip> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    List<Trip> findByStatusIn(List<TripStatus> statuses);

    @Query("""
        SELECT DISTINCT t FROM Trip t
        LEFT JOIN FETCH t.busAssignment ba
        LEFT JOIN FETCH ba.bus b
        LEFT JOIN FETCH ba.driver d
        LEFT JOIN FETCH t.tripUsers tu
        LEFT JOIN FETCH tu.user u
        WHERE t.status != 'IN_PROGRESS'
    """)
    Page<Trip> findAllCompletedTripsWithDetails(Pageable pageable);


    @Query("""
        SELECT t FROM Trip t
        LEFT JOIN FETCH t.busAssignment ba
        LEFT JOIN FETCH ba.bus b
        WHERE t.status = 'COMPLETE'
        AND ba = :busAssignment
    """)
    Page<Trip> findCompletedTripsByDriver(@Param("busAssignment") BusAssignment busAssignment, Pageable pageable);


    @Query("""
        SELECT t
        FROM Trip t
        WHERE t.busAssignment = :busAssignment
        ORDER BY t.createdAt DESC
        LIMIT 1
    """)
    Trip findLatestTripByBusAssignment(@Param("busAssignment") BusAssignment busAssignment);


    @Query("""
        SELECT COUNT(t) FROM Trip t
        WHERE (CAST(:dateTime AS timestamp) IS NULL OR t.createdAt > :dateTime)
    """)
    int countAllWithScheduleFetched(@Param("dateTime") LocalDateTime dateTime);


    @Query("""
        SELECT COUNT(t) FROM Trip t
        WHERE (CAST(:dateTime AS timestamp) IS NULL OR t.createdAt > :dateTime)
        AND t.status = :status
    """)
    int countAllWithScheduleFetchedAndStatus(@Param("dateTime") LocalDateTime dateTime, @Param("status") TripStatus status);

    @Query("""
        SELECT COUNT(t) FROM Trip t
        WHERE (CAST(:dateTime AS timestamp) IS NULL OR t.createdAt > :dateTime)
        AND (t.actualArrivalTime - t.expectedArrivalTime) > 10
    """)
    int countAllWithScheduleFetchedAndDelayed(LocalDateTime dateTime);

    @Query("""
       SELECT COUNT(t) FROM Trip t
       WHERE t.departureTime
       BETWEEN :departureTimeAfter AND :departureTimeBefore
       AND (t.actualArrivalTime - t.expectedArrivalTime) > 10
    """)
    int countByDepartureTimeBetweenAndDelayed(LocalDateTime departureTimeAfter, LocalDateTime departureTimeBefore);

    @Query("""
       SELECT COUNT(t) FROM Trip t
       WHERE t.departureTime
       BETWEEN :departureTimeAfter AND :departureTimeBefore
       AND (t.actualArrivalTime - t.expectedArrivalTime) > 10
       AND t.scheduleLegBusAssignment.scheduleLeg.fromDestination = :departedFrom
    """)
    int countByDepartureTimeBetweenAndDelayedAndDepartedFrom(@Param("departureTimeAfter") LocalDateTime departureTimeAfter,
                                                             @Param("departureTimeBefore") LocalDateTime departureTimeBefore,
                                                             @Param("departedFrom") Destination departedFrom);

    @Query("""
        SELECT COUNT(t)
        FROM Trip t
        JOIN t.busAssignment ba
        WHERE ba.driver = :driver
        AND t.status IN ('ACTIVE','CREATED')
    """)
    int countActiveTripsByDriver(@Param("driver") User driver);




    default Trip findByIdOrThrow(UUID id) {
        return this.findByIdFetch(id)
                .orElseThrow(() -> new TripNotFoundException("Trip with id " + id + " not found"));
    }

    default Trip findActiveTripByBusOrThrow(UUID busId) {
        return this.findActiveTripByBus(busId)
                .orElseThrow(() -> new TripNotFoundException("No active trip found for bus " + busId));
    }

    default Trip findActiveTripByIdOrThrow(TripStatus tripStatus, UUID id) {
        return this.findByStatusAndId(tripStatus, id)
                .orElseThrow(() -> new TripNotFoundException("Trip with id " + id + " not found"));
    }

    List<Trip> findAllByDepartureTimeBetween(LocalDateTime departureTimeAfter, LocalDateTime departureTimeBefore);

    @Query("SELECT MIN(t.departureTime) FROM Trip t")
    LocalDate getFirstDepartureDate();

    @Query("""
    SELECT COUNT(t) FROM Trip t
        WHERE (CAST(:dateTime AS timestamp) IS NULL OR t.createdAt > :dateTime)
        AND t.status = :status
        AND t.route = :route
    """)
    int countAllWithScheduleFetchedAndStatusAndRoute(LocalDateTime dateTime, TripStatus status, Route route);


    int countByDepartureTimeBetweenAndStatus(LocalDateTime departureTimeAfter, LocalDateTime departureTimeBefore, TripStatus status);

    @Query("""
       SELECT COUNT(t) FROM Trip t
       WHERE t.departureTime
       BETWEEN :departureTimeAfter AND :departureTimeBefore
       AND t.status = :status
       AND t.scheduleLegBusAssignment.scheduleLeg.fromDestination = :departedFrom
    """)
    int countByDepartureTimeBetweenAndStatusAndDepartedFrom(@Param("departureTimeAfter") LocalDateTime departureTimeAfter,
                                                            @Param("departureTimeBefore") LocalDateTime departureTimeBefore,
                                                            @Param("status") TripStatus status,
                                                            @Param("departedFrom") Destination departedFrom);
}
