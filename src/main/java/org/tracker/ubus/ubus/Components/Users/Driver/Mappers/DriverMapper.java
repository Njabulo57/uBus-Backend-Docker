package org.tracker.ubus.ubus.Components.Users.Driver.Mappers;


import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Users.Driver.DTO.Response.BusAssignedResponse;

@Component
public class DriverMapper {


    public BusAssignedResponse toDTO(BusAssignment busAssignment) {

        var bus = busAssignment.getBus();
        var busType = bus.getType().getLabel();
        var activityStatus = bus.getActivityStatus().getLabel();
        var schedule = busAssignment.getDriverSchedule().getLabel();


        var capacity = bus.getCapacity();

        return BusAssignedResponse.builder()
                .busName(bus.getName())
                .busModel(bus.getModel())
                .busRegistrationPlate("")
                .busStatus(activityStatus)
                .busRoute("")
                .busType(busType)
                .schedule(schedule)
                .capacity(capacity)
                .build();
    }
}
