package org.tracker.ubus.ubus.Components.Trips.Trip.Listeners;


import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tracker.ubus.ubus.Components.Trips.Trip.Events.GenerateReportEvent;
import org.tracker.ubus.ubus.Components.Trips.TripReport.Mapper.TripReportMapper;
import org.tracker.ubus.ubus.Components.Trips.TripReport.Repository.TripReportRepository;

@Component
@RequiredArgsConstructor
public class GenerateReportEventListener {

    private final TripReportRepository tripReportRepository;
    private final TripReportMapper tripReportMapper;


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGenerateReportEvent(GenerateReportEvent event) {


        var trip = event.getTrip();
        var driver = event.getDriver();

        var tripReport = this.tripReportMapper.toEntity(trip, driver);
        tripReportRepository.save(tripReport);
    }
}
