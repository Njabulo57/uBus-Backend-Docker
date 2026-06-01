package org.tracker.ubus.ubus.Components.Trips.Trip.Events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

@Getter
public final class GenerateReportEvent extends ApplicationEvent {

    private final User driver;
    private final Trip trip;

    public GenerateReportEvent(Object source ,User driver, Trip trip) {
        super(source);
        this.driver = driver;
        this.trip = trip;
    }

}
