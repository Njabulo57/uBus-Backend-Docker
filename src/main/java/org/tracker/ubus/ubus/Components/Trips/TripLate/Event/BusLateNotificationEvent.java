package org.tracker.ubus.ubus.Components.Trips.TripLate.Event;


import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Trips.TripLate.Entity.TripLate;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.util.Collection;

@Getter
public class BusLateNotificationEvent extends ApplicationEvent {

    private final TripLate lateTrip;
    private final DelayStatus delayStatus;
    private final Collection<User> studentsAndStaff;

    public BusLateNotificationEvent(Object source, TripLate lateTrip, DelayStatus delayStatus, Collection<User> studentsAndStaff) {
        super(source);
        this.lateTrip = lateTrip;
        this.delayStatus = delayStatus;
        this.studentsAndStaff = studentsAndStaff;
    }
}
