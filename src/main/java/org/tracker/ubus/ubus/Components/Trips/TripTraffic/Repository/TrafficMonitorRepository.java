package org.tracker.ubus.ubus.Components.Trips.TripTraffic.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.TripTraffic.Entity.Traffic;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrafficMonitorRepository extends JpaRepository<Traffic, UUID> {


    Optional<Traffic> findByTrip(Trip trip);

    Optional<Traffic> findByTripId(UUID tripId);

    boolean existsByTripId(UUID tripId);
    boolean existsByTrip(Trip trip);


    default Traffic findByTripOrThrow(UUID tripId) {
        return this.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Traffic not found for tripId"));
    }

    default Traffic findByTripOrThrow(Trip trip) {
        return this.findByTrip(trip)
                .orElseThrow(() -> new IllegalArgumentException("Traffic not found for tripId"));
    }

}
