package org.tracker.ubus.ubus.Components.Trips.TripReport.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.TripReport.Entity.TripReport;

import java.util.UUID;


@Repository
public interface TripReportRepository extends JpaRepository<TripReport, UUID> {


}
