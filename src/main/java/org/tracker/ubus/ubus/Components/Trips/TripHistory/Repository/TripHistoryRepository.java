package org.tracker.ubus.ubus.Components.Trips.TripHistory.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.Entity.TripHistoryPoint;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripHistoryRepository extends JpaRepository<TripHistoryPoint, UUID> {

    @Query("""
        SELECT thp FROM TripHistoryPoint thp
        WHERE thp.trip.id IN :tripIds
        ORDER BY thp.timePosted ASC
    """)
    List<TripHistoryPoint> findAllByTripIdInOrderByTimePostedAsc(@Param("tripIds") List<UUID> tripIds);

}
