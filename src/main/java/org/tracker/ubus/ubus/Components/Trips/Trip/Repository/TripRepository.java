package org.tracker.ubus.ubus.Components.Trips.Trip.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Exceptions.TripNotFoundException;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {


    // Check if any trips exist between two dates
    boolean existsByDepartureTimeBetween(LocalDateTime start, LocalDateTime end);

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
        SELECT trip FROM Trip trip
        LEFT JOIN FETCH trip.busAssignment ba
        LEFT JOIN FETCH ba.driver
        LEFT JOIN FETCH ba.bus
        LEFT JOIN FETCH trip.schedule s
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
    List<Trip> findByRoute(@Param("route") Route ute);

    @Query("""
        SELECT DISTINCT t FROM Trip t
        LEFT JOIN FETCH t.busAssignment ba
        LEFT JOIN FETCH t.schedule s
        lEFT JOIN FETCH ba.driver
        LEFT JOIN FETCH ba.bus b
        LEFT JOIN FETCH t.tripUsers tu
        WHERE t.status = :statusParam
    """)
    List<Trip> findByStatus(@Param("statusParam") TripStatus status);


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

    default Trip findActiveTripByBusOrThrow(UUID busId) {
        return this.findActiveTripByBus(busId)
                .orElseThrow(() -> new TripNotFoundException("No active trip found for bus " + busId));
    }


    default Trip findByIdOrThrow(UUID id) {
        return this.findByIdFetch(id)
                .orElseThrow(() -> new TripNotFoundException("Trip with id " + id + " not found"));
    }

    default Trip findActiveTripByIdOrThrow(TripStatus tripStatus, UUID id) {
        return this.findByStatusAndId(tripStatus, id)
                .orElseThrow(() -> new TripNotFoundException("Trip with id " + id + " not found"));
    }

    List<Trip> findByCreatedAtAfter(LocalDateTime createdAt);

    @Query("""
    SELECT t
    FROM Trip t
    LEFT JOIN FETCH t.schedule s
    WHERE t.schedule IS NOT NULL
    AND (:dateTime IS NULL OR t.createdAt > :dateTime)
    """)
    List<Trip> findAllWithScheduleFetched(@Param("dateTime") LocalDateTime dateTime);

    @Query("""
    SELECT t
    FROM Trip t
    WHERE t.busAssignment = :busAssignment
    ORDER BY t.createdAt DESC
    LIMIT 1
    """)
    Trip findLatestTripByBusAssignment(@Param("busAssignment") BusAssignment busAssignment);

    @Query("""
    SELECT COUNT(t)
    FROM Trip t
    JOIN t.busAssignment ba
    WHERE ba.driver = :driver
    AND t.status IN ('ACTIVE','CREATED')
    """)
    int countActiveTripsByDriver(@Param("driver") User driver);

    List<Trip> findByDepartureTimeBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);
}
