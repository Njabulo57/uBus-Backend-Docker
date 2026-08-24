package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleLeg;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduleLegRepository extends JpaRepository<ScheduleLeg, UUID> {


    @Query("SELECT DISTINCT sl FROM ScheduleLeg sl " +
            "LEFT JOIN FETCH sl.busesAssigned ba " +
            "LEFT JOIN FETCH ba.bus " +
            "LEFT JOIN FETCH sl.schedule s")
    List<ScheduleLeg> findAllWithEverythingEagerly();

    @Query("""
    SELECT sl FROM ScheduleLeg sl
        LEFT JOIN FETCH sl.schedule s
        LEFT JOIN FETCH sl.busesAssigned ba
        LEFT JOIN FETCH ba.bus b
    WHERE (sl.fromDestination = COALESCE(:from, sl.fromDestination))
    AND (sl.toDestination = COALESCE(:to, sl.toDestination))
    AND (sl.dayOfWeek = COALESCE(:dayOfWeek, sl.dayOfWeek))
    AND (sl.departureTime >= COALESCE(:fromTime, sl.departureTime))
    AND s.validFromDate <= CURRENT_DATE
    AND s.validToDate >= CURRENT_DATE
""")
    Page<ScheduleLeg> findSchedulesWithFilters(
            @Param("from") Destination from,
            @Param("to") Destination to,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("fromTime") LocalTime fromTime,
            Pageable pageable
    );

}