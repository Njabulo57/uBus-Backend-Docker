package org.tracker.ubus.ubus.Components.Trips.Trip.Listeners.Socket;


import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.AbstractListeners.AbstractSocketListener;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.TripUserOnBoardSocketResponse;
import org.tracker.ubus.ubus.Components.Trips.Trip.Events.TripUserOnTappedCardEvent;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.Schedule;

import java.time.format.DateTimeFormatter;


@Slf4j
@Component
public class TripUserOnTappedEventListener extends AbstractSocketListener<TripUserOnBoardSocketResponse> {


    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    public TripUserOnTappedEventListener(SimpMessagingTemplate simpMessagingTemplate) {
        super(simpMessagingTemplate, "/topic/trip-user-on-tapped");
    }


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTapped(TripUserOnTappedCardEvent event) {

        log.info("Received Transactional Trip User On Tapped Event");
        var jwtToken = event.getJwtToken();
        var schedule = event.getTrip()
                .getSchedule();

        log.info("Sending Trip User On Tapped Event to Socket on Thread: {}", Thread.currentThread());
        var tripUserOnBoardResponse = buildResponse(schedule);
        this.sendMessage("/" + jwtToken, tripUserOnBoardResponse);

        log.info("Sent Trip User On Tapped Event");

    }

    private TripUserOnBoardSocketResponse buildResponse(Schedule schedule) {

        var formattedArrivalTime = schedule.getArrivalTime()
                .format(formatter);
        var formattedDepartureTime = schedule.getDepartureTime()
                .format(formatter);

        var route = schedule.getRoute()
                .getLabel();

        return TripUserOnBoardSocketResponse.builder()
                .from(schedule.getFromDestination())
                .to(schedule.getToDestination())
                .arrivalTime(formattedArrivalTime)
                .departureTime(formattedDepartureTime)
                .route(route)
                .build();
    }

}
