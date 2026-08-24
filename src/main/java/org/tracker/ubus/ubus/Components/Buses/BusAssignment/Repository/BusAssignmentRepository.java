package org.tracker.ubus.ubus.Components.Buses.BusAssignment.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Enum.DriverSchedule;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Exceptions.Internal.BusAssignmentNotFoundException;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusAssignmentRepository  extends JpaRepository<BusAssignment, UUID> {

    long countByDriverSchedule(DriverSchedule schedule);

    long countByBus(Bus bus);


    void deleteByBusId(UUID busId);

    @Query("""
        SELECT COUNT(ba) FROM BusAssignment ba
        WHERE ba.bus.id = :busId AND ba.bus.isActive = TRUE
    """)
    long countByBusId(UUID busId);


    Optional<BusAssignment> findByBusAndDriver(Bus bus, User user);

    @Query("""
        SELECT ba FROM BusAssignment ba
        LEFT JOIN FETCH ba.bus b
        LEFT JOIN FETCH ba.driver
        WHERE ba.driver.id = :driverId AND b.isActive = TRUE
    """)
    Optional<BusAssignment> findByDriver(@Param("driverId") UUID driverId);


    Optional<BusAssignment> findByBusAndDriverSchedule(Bus bus, DriverSchedule schedule);


    boolean existsByBusAndDriverSchedule(Bus bus, DriverSchedule schedule);

    boolean existsByDriver(User DRIVER);


    void deleteByBus(Bus bus);

    default BusAssignment findByDriverOrThrow(User driver) {
        return this.findByDriver(driver.getId())
                .orElseThrow(() -> new BusAssignmentNotFoundException("Driver not assigned to any bus"));
    }

    default BusAssignment findByDriverOrThrow(UUID id) {
        return this.findByDriver(id)
                .orElseThrow(() -> new BusAssignmentNotFoundException("Driver not assigned to any bus"));
    }

    default BusAssignment findByBusAndDriverOrThrow(Bus bus, User driver) {
        return this.findByBusAndDriver(bus, driver)
                .orElseThrow(() -> new BusAssignmentNotFoundException("Driver not assigned to any bus"));
    }


    @Query("""
        SELECT ba FROM BusAssignment ba
        left join fetch ba.bus b
        WHERE b.name = :busName
    """)
    List<BusAssignment> findByBusName(@Param("busName") String selectedBusName);
}
