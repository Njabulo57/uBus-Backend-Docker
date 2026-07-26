package org.tracker.ubus.ubus.Components.Trips.TripLunch.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Trips.TripLunch.Entity.TripLunch;

import java.util.UUID;


@Repository
public interface TripLunchRepository extends JpaRepository<TripLunch, UUID> {

}
