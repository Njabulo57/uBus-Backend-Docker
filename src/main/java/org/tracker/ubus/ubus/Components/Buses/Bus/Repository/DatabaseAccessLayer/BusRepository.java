package org.tracker.ubus.ubus.Components.Buses.Bus.Repository.DatabaseAccessLayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusActivityStatus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusOperationalStatus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusType;
import org.tracker.ubus.ubus.Components.Buses.Bus.Exceptions.BusNotFoundException;
import org.tracker.ubus.ubus.Components.Buses.Bus.Repository.Projections.BusWithAssignmentView;
import org.tracker.ubus.ubus.Components.Buses.Bus.Repository.Projections.DriverWithOrWithoutAssignmentView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusRepository extends JpaRepository<Bus, UUID> {


    Optional<Bus> findByName(String name);


    @Query("""
        SELECT b FROM Bus b
        LEFT JOIN FETCH b.busAssignments
    """)
    List<Bus> findAllBuses();

    boolean existsByName(String name);

    List<Bus> findByType(BusType type);



    List<Bus> findAllByIsActiveTrue();

    @Query("""
        SELECT b FROM Bus b
        WHERE b.isActive = true
        ORDER BY b.createdAt DESC
    """)
    List<Bus> findAllOrderByLatestCreation();

    @Query("""
        SELECT b FROM Bus b
        WHERE b.isActive = true
        AND b.operationalStatus = :opStatus
        AND b.activityStatus = :actStatus ORDER BY b.createdAt DESC
    """)
    List<Bus> findByStatusCombinationLatest(
            @Param("opStatus") BusOperationalStatus operationalStatus,
            @Param("actStatus") BusActivityStatus activityStatus
    );


    @Query("""
        SELECT DISTINCT d as driver,
            ba.bus bus,
            CASE WHEN ba IS NOT NULL THEN TRUE ELSE FALSE
                END as assigned
        FROM User d
        LEFT JOIN BusAssignment ba ON ba.driver.id = d.id
        LEFT JOIN ba.bus b
        WHERE d.status = 'ACTIVE' AND d.role = 'DRIVER'
    """)
    List<DriverWithOrWithoutAssignmentView> findAssignedBusesOrDefault();


    @Query("""
        SELECT DISTINCT b as bus,
            ba.driver as driver
        FROM Bus b
        LEFT JOIN BusAssignment ba ON ba.bus.id = b.id
        LEFT JOIN ba.driver d
        WHERE b.isActive = TRUE
    """)
    List<BusWithAssignmentView> findAllBusesWithAssignment();


    @Query("SELECT b FROM Bus b LEFT JOIN FETCH b.busAssignments WHERE b.id = :id")
    Bus findByIdWithAssignments(@Param("id") UUID id);

    default Bus findByIdOrThrow(UUID id) {
        return this.findById(id)
                .orElseThrow(() -> new BusNotFoundException("Bus with id " + id + " not found"));
    }

    default Bus findByNameOrThrow(String name)
            throws BusNotFoundException {
        return this.findByName(name)
                .orElseThrow(() -> new BusNotFoundException("Bus with name : " + name + " not found."));
    }

}
