package org.tracker.ubus.ubus.Components.Trips.Trip.Listeners.Socket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.AbstractListeners.AbstractSocketListener;
import org.tracker.ubus.ubus.Components.Trips.Trip.Events.TripBusActivityStatusChangeEvent;

@Component
public class TripBusActivityStatusChangeListener extends AbstractSocketListener<String> {


    public TripBusActivityStatusChangeListener(SimpMessagingTemplate simpMessagingTemplate) {
        super(simpMessagingTemplate, "/topic/trip-bus-activity-status-change");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTripBusActivityStatusChangeEvent(TripBusActivityStatusChangeEvent event){

        var tripId = event.getTripId();
        var busActivityStatus = event.getBusActivityStatus();
        var strBusActivityStatus = busActivityStatus.getLabel()
                .toUpperCase();
        this.sendMessage("/" + tripId, strBusActivityStatus);

    }
}
