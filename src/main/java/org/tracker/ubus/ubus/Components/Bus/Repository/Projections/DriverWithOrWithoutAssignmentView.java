package org.tracker.ubus.ubus.Components.Bus.Repository.Projections;


import org.tracker.ubus.ubus.Components.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.User.Entity.User;

public interface DriverWithOrWithoutAssignmentView {

    User getDriver();

    Bus getBus();
}
