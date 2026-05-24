package org.tracker.ubus.ubus.Components.BusAssignment.Service.Interface;

import java.util.UUID;

public interface IBusAssignmentService {

    void assignDriverToBus(UUID driverId, UUID busId, String schedule);
}
