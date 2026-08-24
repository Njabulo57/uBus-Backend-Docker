package org.tracker.ubus.ubus.Components.Trips.TripUser.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Entity.TripUser;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Enum.TripUserStatus;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TripUserRepository extends JpaRepository<TripUser, UUID> {



    TripUser findByTripAndUser(Trip trip, User user);

    List<TripUser> findAllByTripAndStatus(Trip trip, TripUserStatus status);

    @Query("SELECT COUNT(DISTINCT tu.user.id) FROM TripUser tu")
    int countUniqueUserIds();

    int countByTrip(Trip trip);

    @Query("""
        SELECT COUNT(tu) FROM TripUser tu
        LEFT JOIN tu.trip t
        WHERE tu.user = :user
        AND t.status = 'COMPLETE'
    """)
    int countByUser(User user);

    @Query("""
        SELECT DISTINCT tu FROM TripUser tu
        LEFT JOIN FETCH tu.trip t
        LEFT JOIN FETCH t.busAssignment ba
        LEFT JOIN FETCH ba.driver
        WHERE tu.user = :userParam AND t.status = 'COMPLETE'
    """)
    Page<TripUser> findCompletedTripsByUser(@Param("userParam") User user, Pageable pageable);

    @Query("""
        SELECT tu FROM TripUser  tu
        LEFT JOIN FETCH tu.user
        WHERE tu.trip =:tripParam
    """)
    List<TripUser> findByTrip(@Param("tripParam") Trip trip);


    @Query("""
        SELECT COUNT(DISTINCT tu.user.id) FROM TripUser tu
        WHERE tu.createdAt > :dateTime
    """)
    int countUniqueUserIdsByCreatedAtAfter(LocalDateTime dateTime);

    List<UUID> findUserIdsByTrip(Trip trip);


    List<TripUser> findAllByCreatedAtAfterAndIsFirstTrip(LocalDateTime dateTime, boolean isFirst);

    List<TripUser> findAllByCreatedAtAfterAndStatus(LocalDateTime dateTime, TripUserStatus status);

    List<TripUser> findAllByIsFirstTrip(boolean b);

    List<TripUser> findAllByStatus(TripUserStatus tripUserStatus);

    List<TripUser> findAllByCreatedAtAfter(LocalDateTime dateTime);


    boolean existsByUserAndTrip(User user, Trip trip);
}
