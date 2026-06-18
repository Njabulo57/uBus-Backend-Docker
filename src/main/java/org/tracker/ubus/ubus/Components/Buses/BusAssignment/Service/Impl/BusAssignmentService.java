package org.tracker.ubus.ubus.Components.Buses.BusAssignment.Service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Buses.Bus.Repository.DatabaseAccessLayer.BusRepository;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Enum.DriverSchedule;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Events.BusAssignmentEmailNotificationEvent;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Mappers.BusAssignmentMapper;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Repository.BusAssignmentRepository;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Service.Interface.IBusAssignmentService;
import org.tracker.ubus.ubus.Components.EventHandler.Publisher.MultiEvenPublisher;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class BusAssignmentService implements IBusAssignmentService {


    private final BusRepository busRepository;
    private final MultiEvenPublisher publisher;
    private final UserRepository userRepository;
    private static final int maximumBusAssignments = 2;
    private final BusAssignmentMapper busAssignmentMapper;
    private final BusAssignmentRepository busAssignmentRepository;


    @Override
    @Transactional
    public void assignDriverToBus(UUID driverId, UUID busId, String schedule) {

        DriverSchedule driverSchedule = DriverSchedule.fromLabel(schedule);
        long busAssignments = this.busAssignmentRepository.countByBusId(busId);
        if(busAssignments >= maximumBusAssignments)
            throw new IllegalStateException("Bus already has "+ maximumBusAssignments +
                    " drivers assigned. Maximum is " +
                            maximumBusAssignments);

        var bus = this.busRepository.findByIdOrThrow(busId);
        var driver = this.userRepository.findByIdOrThrow(driverId);


        var newBusAssignment = this.busAssignmentMapper.toEntity(bus, driver, driverSchedule);
        this.busAssignmentRepository.save(newBusAssignment);

        var emailNotificationEvent = new BusAssignmentEmailNotificationEvent(this, newBusAssignment);
        this.publisher.publish(()-> emailNotificationEvent); //let the driver know via email

    }
}
