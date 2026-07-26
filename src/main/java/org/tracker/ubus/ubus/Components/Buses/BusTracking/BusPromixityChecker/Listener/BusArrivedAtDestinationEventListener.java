package org.tracker.ubus.ubus.Components.Buses.BusTracking.BusPromixityChecker.Listener;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.BusPromixityChecker.Event.BusArrivedAtDestinationEvent;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.AbstractListeners.AbstractSocketListener;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Entity.TripUser;

import java.util.Collection;

@Slf4j
@Component
public class BusArrivedAtDestinationEventListener extends AbstractSocketListener<String> {


    public BusArrivedAtDestinationEventListener(SimpMessagingTemplate simpMessagingTemplate) {
        super(simpMessagingTemplate, "/topic/bus-arrived-at-destination/" );
    }


    @EventListener
    public void onBusArrivedAtDestination(BusArrivedAtDestinationEvent event) {
        var message = "Bus Arrived at Destination. Don't forget to tap out of the bus";
        var trips = event.getTrips();



        trips.forEach(trip -> {
            //for every trip send the message to all users on the trip
            var tripUsers = trip.getTripUsers();

            log.info("Trip {} has arrived at destination", trip.getBusAssignment().getBus().getName());
            this.sendToUsersOnBus(tripUsers, message); // let the users know that the bus has arrived at the destination
        });

    }

    private void sendToUsersOnBus(Collection<TripUser> usersOnTrip, String message) {
        usersOnTrip.forEach(tripUser -> {
            var user = tripUser.getUser();
            var userId = user.getId();
            this.sendMessage("user/" + userId , message);
        });
    }

}
