package org.tracker.ubus.ubus.Components.Buses.BusTracking.BusPromixityChecker.Listener;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.BusPromixityChecker.Event.BusJourneyProgressionEvent;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.AbstractListeners.AbstractSocketListener;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Entity.TripUser;

import java.util.Collection;

@Slf4j
@Component
public class BusJourneyProgressionEventListener extends AbstractSocketListener<String> {

    public BusJourneyProgressionEventListener(SimpMessagingTemplate simpMessagingTemplate) {
        super(simpMessagingTemplate, "/topic/bus-journey-progression/" );
    }


    @EventListener
    public void onBusArrivedAtDestination(BusJourneyProgressionEvent event) {
        var trips = event.getTripProgressions();

        trips.forEach(tripProgression -> {
            //for every trip send the message to all users on the trip
            var tripUsers = tripProgression.trip()
                    .getTripUsers();

            this.sendToUsersOnBus(tripUsers, tripProgression.message()); // let the users know that the bus has arrived at the destination
        });

    }

    private void sendToUsersOnBus(Collection<TripUser> usersOnTrip, String message) {
        usersOnTrip.forEach(tripUser -> {
            var user = tripUser.getUser();
            var email = user.getEmail();
            this.sendMessage("user/" +  email, message);
        });
    }

}
