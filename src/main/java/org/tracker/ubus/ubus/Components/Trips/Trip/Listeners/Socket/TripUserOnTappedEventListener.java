package org.tracker.ubus.ubus.Components.Trips.Trip.Listeners.Socket;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.AbstractListeners.AbstractSocketListener;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.TripUser.DTOs.Response.UserOnTripResponse;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Entity.TripUser;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Event.UserOnTripEvent;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.util.Collection;


@Slf4j
@Component
public class TripUserOnTappedEventListener extends AbstractSocketListener<UserOnTripResponse> {


    public TripUserOnTappedEventListener(SimpMessagingTemplate simpMessagingTemplate) {
        super(simpMessagingTemplate, "/topic/trip-user-on-tapped/");
    }


    @EventListener
    public void onTapped(UserOnTripEvent event) {


        log.info("Received Transactional Trip User On Tapped Event");
        log.info("Sending Trip User On Tapped Event to Socket on Thread: {}", Thread.currentThread());

        var to = getDestination(event.getTrip(), true);
        log.info("Sending Trip User On Tapped Event {}. going to {}", event.getTrip().countPassengers(), to);


        var response = constructResponse(event);
        log.info("PAYLOAD {}", response);
        var passengers = getTripPassengers(event.getTrip());

        this.sendMessage(passengers, response);
    }


    private Collection<User> getTripPassengers(Trip trip) {
        return trip.getTripUsers().stream()
                .map(TripUser::getUser)
                .toList();
    }

    private UserOnTripResponse constructResponse(UserOnTripEvent event) {

        var to = getDestination(event.getTrip(), true);
        var from = getDestination(event.getTrip(), false);


        var delayStatus = event.getDelayStatus();

        return UserOnTripResponse.builder()
                .progressBar(event.getProgressBar())
                .distance(event.getDistance())
                .speed(event.getSpeed())
                .isCompleted(event.isCompleted())
                .to(to)
                .from(from)
                .proximityMessage(event.getProximityMessage())
                .delayStatus(delayStatus)
                .build();
    }

    private Destination getDestination(Trip trip, boolean isGettingTo) {

        var scheduleLeg = trip.getScheduleLegBusAssignment()
                .getScheduleLeg();
        if(isGettingTo)
            return scheduleLeg.getToDestination();
        return scheduleLeg.getFromDestination();
    }

}
