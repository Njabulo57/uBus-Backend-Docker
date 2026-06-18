package org.tracker.ubus.ubus.Components.Buses.BusRoute.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.BusRoute.Entity.BusRoute;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusRouteRepository extends JpaRepository<BusRoute, UUID> {

    @Query("""
        SELECT br FROM BusRoute br
        LEFT JOIN FETCH  br.bus
    """)
    List<BusRoute> findByRoute(Route route);

    Optional<BusRoute> findByBus(Bus bus);


    default BusRoute findByBusOrThrow(Bus bus) {
        return this.findByBus(bus)
                .orElseThrow(() -> new IllegalArgumentException("BusRoute not found for bus " + bus.getId()));
    }

}
