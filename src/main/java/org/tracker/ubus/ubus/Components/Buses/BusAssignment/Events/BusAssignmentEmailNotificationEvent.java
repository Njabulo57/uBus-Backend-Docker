package org.tracker.ubus.ubus.Components.Buses.BusAssignment.Events;

import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.EventHandler.AbstractEvents.EmailEvent;
import org.tracker.ubus.ubus.Components.Shared.Mail.Templates.Bus.BusAssignmentTemplate;


public class BusAssignmentEmailNotificationEvent extends EmailEvent {

   private final BusAssignment busAssignment;


    public BusAssignmentEmailNotificationEvent(Object source, BusAssignment busAssignment) {
        super(source);
        this.busAssignment = busAssignment;
        setBody(constructHtmlBody());
        setHeader("Bus Assignment Notification");


        var subject = busAssignment.getDriver().getEmail();
        setToEmail(subject);
    }

    @Override
    protected String constructHtmlBody() {

        var driver = busAssignment.getDriver();
        var driverSchedule = busAssignment.getDriverSchedule();
        var bus =  busAssignment.getBus();
        return BusAssignmentTemplate.getTemplate(driver, bus, driverSchedule);
    }
}
