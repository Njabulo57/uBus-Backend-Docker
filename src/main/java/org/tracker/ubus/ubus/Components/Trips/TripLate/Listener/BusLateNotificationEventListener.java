package org.tracker.ubus.ubus.Components.Trips.TripLate.Listener;


import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.AbstractListeners.AbstractSocketListener;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Trips.TripLate.DTO.Response.LateReasonResponse;
import org.tracker.ubus.ubus.Components.Trips.TripLate.Enum.TripLateReason;
import org.tracker.ubus.ubus.Components.Trips.TripLate.Event.BusLateNotificationEvent;

@Component

public class BusLateNotificationEventListener extends AbstractSocketListener<LateReasonResponse> {


    public BusLateNotificationEventListener(SimpMessagingTemplate simpMessagingTemplate) {
        super(simpMessagingTemplate, "/topic/bus-late-notification/");
    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBusLateNotificationEvent(BusLateNotificationEvent event) {


        var studentsAndStaff = event.getStudentsAndStaff();
        var lateTripEntity = event.getLateTrip();
        var tripEntity = lateTripEntity.getTrip();
        var reasonLate = lateTripEntity.getReason();

        var delayStatus = event.getDelayStatus();


        var reasonToSend = constructMessage(lateTripEntity.getReason(), delayStatus);
        if(reasonLate.equals(TripLateReason.OTHER)
            && lateTripEntity.getDescription() != null
            && !lateTripEntity.getDescription().isBlank())
                reasonToSend = lateTripEntity.getDescription();

        var response = constructResponse(lateTripEntity.getReason(), delayStatus, reasonToSend);

        this.sendMessage("tripId/" + tripEntity.getId(), response,
                studentsAndStaff);

    }


    private String constructMessage(TripLateReason reasonLate, DelayStatus delayStatus) {
        var minutesDelay = delayStatus.delayInMinutes();
        var arrivalTime = delayStatus.arrivalTime();
        return reasonLate.name() + ". Bus Will Arrive " + minutesDelay + " Later. At " + arrivalTime;
    }

    private LateReasonResponse constructResponse(TripLateReason reasonLate, DelayStatus delayStatus,
                                                 String description) {

        var delayMinutes = (int) delayStatus.delayInMinutes();
        return LateReasonResponse.builder()
                .reason(reasonLate.name())
                .message(description)
                .delayMinutes(delayMinutes)
                .lateArrivalTime(delayStatus.arrivalTime())
                .build();


    }
}

