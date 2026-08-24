package org.tracker.ubus.ubus.Components.Buses.BusPreference.Listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPreferenceClosestTripResponse;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Events.BusPreferenceSubscriberAllTripsEvent;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.AbstractListeners.AbstractSocketListener;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.time.LocalTime;
import java.util.Collection;
import java.util.SequencedCollection;


@Slf4j
@Component
public class BusPreferenceSubscriberAllTripsEventListener extends AbstractSocketListener<BusPreferenceClosestTripResponse> {

    public BusPreferenceSubscriberAllTripsEventListener(SimpMessagingTemplate simpMessagingTemplate) {
        super(simpMessagingTemplate, "/topic/bus-preference-suscriber-all-trips/");
    }

    /**
     * Handles the event for broadcasting the status of all trips to users who have subscribed for updates.
     *
     * @param event the event containing trip details, schedule, progress, user destinations, and other related information
     */
    @EventListener
    public void handleSendAllTripStatuses(BusPreferenceSubscriberAllTripsEvent event) {

        var schedule = event.getTrip()
                .getScheduleLegBusAssignment()
                .getScheduleLeg();

        var trip = event.getTrip();

        var route = trip.getRoute();
        var uniqueStops = route.getUniqueStops();
        var nextStop = findNextDestination(uniqueStops, schedule.getToDestination());

        var bus = trip.getBusAssignment().getBus();
        var totalSeatsAvailable = this.getAvailableSeats(bus, trip);
        var totalSeats = bus.getCapacity();

        var minsLeft = this.getMinutesToDest(event.getDelayStatus());

        var progress = event.getProgress();
        var response = BusPreferenceClosestTripResponse.builder()
                .eta(event.getEta() + " To " + schedule.getToDestination())
                .distance(event.getDistanceInKM())
                .from("Coming From: " + schedule.getFromDestination())
                .to("Going To: " + schedule.getToDestination() + " Next: " + nextStop)
                .progress(progress)
                .busName("Bus: "+bus.getRegistrationNumber())
                .totalSeatsAvailable(totalSeatsAvailable)
                .totalSeats(totalSeats)
                .progress(event.getProgress())
                .delayStatus(event.getDelayStatus())
                .minutesLeft(minsLeft)
                .stops(0)
                .tripId(trip.getId())
                .build();

        var allUsers = event.getUsers();
        this.sendToALL(allUsers, response); //send to all users
    }


    private Destination findNextDestination(SequencedCollection<Destination> destinations, Destination current)
        throws IllegalStateException {
        var iterator = destinations.iterator();
        while (iterator.hasNext()) {
            var nextDest = iterator.next();
            if (nextDest == current)
                return iterator.hasNext() ? iterator.next() :
                        destinations.getFirst();
        }

        //not suppose to happen
        throw new IllegalStateException("No next destination found for " + current);
    }

    private String getMinutesToDest(DelayStatus delayStatus) {

        var currentTime = LocalTime.now();
        var eta = delayStatus.eta();

        var difference = currentTime.getMinute() - eta.getMinute();
        if(difference == 0)
            return "Arrived";
        else
            return String.valueOf(difference);
    }


    private int getAvailableSeats(Bus bus, Trip trip) {
        return bus.getCapacity() - trip.countPassengers();
    }

    private void sendToALL(Collection<User> users, BusPreferenceClosestTripResponse response) {
        users.forEach(user ->
                this.sendMessage(user.getEmail(), response));
    }
}
