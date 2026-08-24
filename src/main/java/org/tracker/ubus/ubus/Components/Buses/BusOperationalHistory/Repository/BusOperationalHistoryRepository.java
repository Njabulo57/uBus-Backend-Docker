package org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Entity.BusOperationalHistory;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Enum.MaintenanceIssue;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Enum.Priority;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BusOperationalHistoryRepository extends JpaRepository<BusOperationalHistory, UUID> {


    @Query("""
        SELECT boh FROM BusOperationalHistory boh
            LEFT JOIN FETCH  boh.bus
        WHERE boh.priority =:priority
    """)
    List<BusOperationalHistory> findByPriority(@Param("priority") Priority priority);

    @Query("""
        SELECT boh FROM BusOperationalHistory boh
            LEFT JOIN FETCH  boh.bus
        WHERE boh.maintenanceIssue =:issue
    """)
    List<BusOperationalHistory> findByMaintenanceIssue(@Param("issue") MaintenanceIssue issue);


    List<BusOperationalHistory> findByMaintenanceIssueIn(List<MaintenanceIssue> issues);


    List<BusOperationalHistory> findByBusAndMaintenanceIssueIn(Bus bus, List<MaintenanceIssue> issues);

    List<BusOperationalHistory> findByBusAndPriorityAndDateOperated(Bus bus, Priority priority, LocalDate dateOperated);


    @Query("""
        SELECT boh FROM BusOperationalHistory boh
            LEFT JOIN FETCH boh.bus
        WHERE boh.dateOperated BETWEEN :startDate AND :endDate
        ORDER BY boh.dateOperated DESC
    """)
    List<BusOperationalHistory> findByDateOperatedBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

}
