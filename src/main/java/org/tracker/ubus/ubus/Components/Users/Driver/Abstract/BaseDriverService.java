package org.tracker.ubus.ubus.Components.Users.Driver.Abstract;


import org.springframework.beans.factory.annotation.Autowired;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Repository.BusAssignmentRepository;
import org.tracker.ubus.ubus.Components.Shared.Entities.BaseService;


public abstract class BaseDriverService extends BaseService {

    @Autowired
    protected BusAssignmentRepository busAssignmentRepository;

    protected Bus getDriverBus() {
        var driver = getCurrentUser();
        var busAssignment = this.busAssignmentRepository.findByDriverOrThrow(driver);
        return busAssignment
                .getBus();
    }

    protected BusAssignment getDriverBusAssignment() {
        var driver = getCurrentUser();
        var busAssignment = this.busAssignmentRepository.findByDriverOrThrow(driver);
        return busAssignment;
    }
}
