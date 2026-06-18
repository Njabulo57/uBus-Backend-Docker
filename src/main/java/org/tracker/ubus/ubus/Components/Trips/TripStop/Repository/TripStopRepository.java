package org.tracker.ubus.ubus.Components.Trips.TripStop.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.TripStop.Entity.TripStop;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripStopRepository extends JpaRepository<TripStop, UUID> {

    List<TripStop> findByTrip(Trip trip);

    @Query("""
        SELECT ts FROM TripStop ts
        WHERE ts.trip.id = : tridId
    """)
    List<Trip> findByTrip(@Param("tripId") UUID tripId);
}
