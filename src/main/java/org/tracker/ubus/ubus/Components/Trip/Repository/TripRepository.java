package org.tracker.ubus.ubus.Components.Trip.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trip.Enum.TripRoute;
import org.tracker.ubus.ubus.Components.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trip.Exceptions.TripNotFoundException;
import org.tracker.ubus.ubus.Components.User.Enum.Route;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {


    @Query("""
        SELECT trip FROM Trip trip
        LEFT JOIN FETCH trip.busAssignment ba
        LEFT JOIN FETCH ba.bus
        WHERE trip.id = :id
    """)
    Optional<Trip> findByIdFetch(@Param("id") UUID id);


    @Query("""
        SELECT t  FROM Trip t
        LEFT JOIN t.busAssignment ba
        LEFT JOIN ba.bus b
        WHERE t.status = 'IN_PROGRESS'
        AND b.id =: busId
    """)
    Optional<Trip> findActiveTripByBus(@Param("busId") UUID busId);

    @Query("""
        SELECT t FROM Trip t
        LEFT JOIN t.busAssignment ba
        LEFT JOIN ba.driver
        WHERE t.status = 'IN_PROGRESS'
        AND t.route = : route
    """)
    List<Trip> findByRoute(@Param("route") TripRoute route);

    @Query("""
        SELECT t FROM Trip t
        LEFT JOIN t.busAssignment ba
        LEFT JOIN ba.bus b
        WHERE t.status =: status
    """)
    List<Trip> findByStatus(@Param("status") TripStatus status);


    default Trip findActiveTripByBusOrThrow(UUID busId) {
        return this.findActiveTripByBus(busId)
                .orElseThrow(() -> new TripNotFoundException("No active trip found for bus " + busId));
    }

    default Trip findByIdOrThrow(UUID id) {
        return this.findByIdFetch(id)
                .orElseThrow(() -> new TripNotFoundException("Trip with id " + id + " not found"));
    }
}
