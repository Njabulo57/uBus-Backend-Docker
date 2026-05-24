package org.tracker.ubus.ubus.Components.Trip.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trip.Exceptions.TripNotFoundException;

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

    default Trip findByIdOrThrow(UUID id) {
        return this.findByIdFetch(id)
                .orElseThrow(() -> new TripNotFoundException("Trip with id " + id + " not found"));
    }
}
