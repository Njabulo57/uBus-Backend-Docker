package org.tracker.ubus.ubus.Components.Trips.Trip.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusOperationalStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TripReportsRepository extends TripRepository {


    @Query("""
        SELECT DISTINCT t FROM Trip t
        LEFT JOIN FETCH t.scheduleLegBusAssignment slba
        LEFT JOIN FETCH slba.bus
        LEFT JOIN FETCH t.busAssignment ba
        LEFT JOIN FETCH ba.bus
        WHERE t.route = :route
        AND DATE(t.departureTime) BETWEEN :startDate AND :endDate
        AND t.status = :status
        ORDER BY t.departureTime
    """)
    List<Trip> findByRouteAndDateRange(
            @Param("route") Route route,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") TripStatus status
    );


    //counts passengers per route per day
    @Query("""
        SELECT CAST( t.departureTime AS Localdate),
               t.route,
               COUNT(tu)
        FROM Trip t
        LEFT JOIN t.tripUsers tu
        WHERE t.departureTime IS NOT NULL
        GROUP BY CAST(t.departureTime AS LocalDate), t.route
        ORDER BY CAST(t.departureTime AS LocalDate), t.route
    """)
    List<Object[]> findPassengersPerRoutePerDay();

    // scheduled trips per day
    @Query("""
    SELECT CAST(t.departureTime AS LocalDate),
           t.route,
           COUNT(t)
    FROM Trip t
    WHERE t.departureTime IS NOT NULL
   
    GROUP BY CAST(t.departureTime AS LocalDate), t.route
    ORDER BY CAST(t.departureTime AS LocalDate), t.route
""")
    List<Object[]> findDailyScheduledTripsByRoute();


    // buses used per day
    @Query("""
    SELECT CAST(t.departureTime AS LocalDate),
           t.route,
           COUNT(DISTINCT t.busAssignment.bus.id)
    FROM Trip t
    WHERE t.departureTime IS NOT NULL
    GROUP BY CAST(t.departureTime AS LocalDate), t.route
    ORDER BY CAST(t.departureTime AS LocalDate), t.route
""")
    List<Object[]> findDailyBusesUsedByRoute();


    @Query("""
    SELECT boh.dateOperated,
           b.route,
           COUNT(boh.bus)
    FROM BusOperationalHistory boh
    JOIN boh.bus b
    WHERE boh.busOperationalStatus = :status
    GROUP BY boh.dateOperated, b.route
    ORDER BY boh.dateOperated, b.route
""")
    List<Object[]> findDailyAvailableBusesByRoute(@Param("status") BusOperationalStatus status);

    // available buses per day
    @Query("""
    SELECT boh.dateOperated,
           COUNT(boh.bus)
    FROM BusOperationalHistory boh
    WHERE boh.busOperationalStatus = :status
    GROUP BY boh.dateOperated
    ORDER BY boh.dateOperated
""")
    List<Object[]> findDailyAvailableBuses(@Param("status") BusOperationalStatus status);

}
