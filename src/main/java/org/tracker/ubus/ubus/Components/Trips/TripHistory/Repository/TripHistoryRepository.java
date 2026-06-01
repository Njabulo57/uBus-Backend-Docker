package org.tracker.ubus.ubus.Components.Trips.TripHistory.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.Entity.TripHistoryPoint;
import java.util.UUID;

@Repository
public interface TripHistoryRepository extends JpaRepository<TripHistoryPoint, UUID> {
}
