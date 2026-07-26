package org.tracker.ubus.ubus.Components.Buses.Bus.Repository.Projections;

import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

public interface BusWithAssignmentView {

    Bus getBus();

    User getDriver();
}
