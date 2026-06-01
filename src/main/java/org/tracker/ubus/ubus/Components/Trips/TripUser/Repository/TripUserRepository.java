package org.tracker.ubus.ubus.Components.Trips.TripUser.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Entity.TripUser;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.util.UUID;

@Repository
public interface TripUserRepository extends JpaRepository<TripUser, UUID> {


    int countByTrip(Trip trip);


    @Query("""
        SELECT tu FROM TripUser tu
        LEFT JOIN FETCH tu.trip t
        LEFT JOIN FETCH  t.busAssignment ba
        LEFT JOIN FETCH ba.driver
        WHERE tu.user = :userParam AND t.status ='COMPLETE'
    """)
    Page<TripUser> findCompletedTripsByUser(@Param("userParam") User user, Pageable pageable);
}
