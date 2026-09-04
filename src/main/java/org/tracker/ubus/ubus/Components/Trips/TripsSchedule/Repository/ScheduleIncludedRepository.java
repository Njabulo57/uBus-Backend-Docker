package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.Schedule;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleDatesExcluded;

import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

@Repository
public interface ScheduleIncludedRepository extends JpaRepository<ScheduleDatesExcluded, UUID> {

    @Query("""
        SELECT DISTINCT sde FROM ScheduleDatesExcluded sde
        WHERE sde.fromDate <= :endDate
        AND sde.toDate >= :startDate
    """)
    Collection<ScheduleDatesExcluded> findByExcludedDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    Collection<ScheduleDatesExcluded> findBySchedule(Schedule schedule);
}