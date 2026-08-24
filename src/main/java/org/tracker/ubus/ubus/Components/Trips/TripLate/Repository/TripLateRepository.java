package org.tracker.ubus.ubus.Components.Trips.TripLate.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.TripLate.Entity.TripLate;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface TripLateRepository extends JpaRepository<TripLate, UUID> {
    TripLate findByTrip(Trip trip);

    Optional<TripLate> findByTripId(UUID tripId);


    default TripLate findByTripIdOrThrow(UUID tripId) {
        return this.findByTripId(tripId)
                .orElseThrow(() -> new IllegalArgumentException("TripLate not found for tripId"));
    }

}
