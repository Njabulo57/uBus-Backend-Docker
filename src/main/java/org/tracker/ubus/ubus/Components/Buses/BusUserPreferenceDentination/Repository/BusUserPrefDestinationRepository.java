package org.tracker.ubus.ubus.Components.Buses.BusUserPreferenceDentination.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Buses.BusUserPreferenceDentination.Entity.BusUserPreferenceDestination;

import java.util.UUID;


@Repository
public interface BusUserPrefDestinationRepository extends JpaRepository<BusUserPreferenceDestination, UUID> {
}
