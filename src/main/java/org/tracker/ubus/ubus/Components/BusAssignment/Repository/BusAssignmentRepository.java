package org.tracker.ubus.ubus.Components.BusAssignment.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.BusAssignment.Enum.DriverSchedule;

import java.util.UUID;

@Repository
public interface BusAssignmentRepository  extends JpaRepository<BusAssignment, UUID> {

    long countByDriverSchedule(DriverSchedule schedule);
    long countByBus(Bus bus);
    long countByBusId(UUID busId);

    @Query("SELECT b FROM Bus b LEFT JOIN FETCH b.busAssignments WHERE b.id = :id")
    Bus findByIdWithAssignments(@Param("id") UUID id);

    boolean existsByBusAndDriverSchedule(Bus bus, DriverSchedule schedule);
}
