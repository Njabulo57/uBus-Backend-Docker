package org.tracker.ubus.ubus.Components.Trips.Trip.Listeners.Socket;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.AbstractListeners.AbstractSocketListener;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.TripUserOnBoardSocketResponse;
import org.tracker.ubus.ubus.Components.Trips.Trip.Events.TripUserOnTappedCardEvent;
import org.tracker.ubus.ubus.Components.Trips.TripUser.DTOs.Response.UserOnTripResponse;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Event.UserOnTappedTripEvent;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.Schedule;

import java.time.format.DateTimeFormatter;


@Slf4j
@Component
public class TripUserOnTappedEventListener extends AbstractSocketListener<UserOnTripResponse> {


    public TripUserOnTappedEventListener(SimpMessagingTemplate simpMessagingTemplate) {
        super(simpMessagingTemplate, "/topic/trip-user-on-tapped");
    }


    @EventListener
    public void onTapped(UserOnTappedTripEvent event) {

        var trip = event.getTrip();

        var response = constructResponse(event);
        this.sendMessage("/" + trip.getId() , response);
    }


    private UserOnTripResponse constructResponse(UserOnTappedTripEvent event) {
        var delayStatus = event.getDelayStatus();
        return UserOnTripResponse.builder()
                .isCompleted(event.isCompleted())
                .from(event.getFrom())
                .to(event.getTo())
                .proximityMessage(event.getProximityMessage())
                .delayStatus(delayStatus)
                .build();

    }


}
