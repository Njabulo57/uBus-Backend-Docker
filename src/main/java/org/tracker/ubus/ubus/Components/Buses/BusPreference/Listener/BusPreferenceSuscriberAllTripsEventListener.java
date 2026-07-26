package org.tracker.ubus.ubus.Components.Buses.BusPreference.Listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPreferenceClosestTripResponse;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Events.BusPreferenceSuscriberAllTripsEvent;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.AbstractListeners.AbstractSocketListener;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.math.BigDecimal;
import java.util.Collection;


@Slf4j
@Component
public class BusPreferenceSuscriberAllTripsEventListener extends AbstractSocketListener<BusPreferenceClosestTripResponse> {


    public BusPreferenceSuscriberAllTripsEventListener(SimpMessagingTemplate simpMessagingTemplate) {
        super(simpMessagingTemplate, "/topic/bus-preference-suscriber-all-trips/");
    }


    @Async
    @EventListener
    public void handleSendAllTripStatuses(BusPreferenceSuscriberAllTripsEvent event) {

        var schedule = event.getTrip()
                .getSchedule();

        var bus = schedule.getBus();
        var trip = event.getTrip();

        var response = BusPreferenceClosestTripResponse.builder()
                .eta(event.getEta() + " To " + schedule.getFromDestination())
                .distance(event.getDistanceInKM())
                .from(schedule.getFromDestination())
                .to(schedule.getToDestination())
                .busName(bus.getName())
                .delayStatus(event.getDelayStatus())
                .stops(0)
                .tripId(trip.getId())
                .build();

        var allUsers = event.getUsers();
        this.sendToALL(allUsers, response); //send to all users
    }

    private void sendToALL(Collection<User> users, BusPreferenceClosestTripResponse response) {
        users.forEach(user ->
                this.sendMessage(user.getEmail(), response));
    }
}
