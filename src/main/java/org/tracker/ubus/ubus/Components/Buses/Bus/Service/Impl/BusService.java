package org.tracker.ubus.ubus.Components.Buses.Bus.Service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Buses.Bus.DTOs.Requests.BusEditRequest;
import org.tracker.ubus.ubus.Components.Buses.Bus.DTOs.Requests.BusRegisterRequest;
import org.tracker.ubus.ubus.Components.Buses.Bus.DTOs.Responses.BusAdminViewResponse;
import org.tracker.ubus.ubus.Components.Buses.Bus.DTOs.Responses.BusRegisterResponse;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusActivityStatus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusOperationalStatus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusType;
import org.tracker.ubus.ubus.Components.Buses.Bus.Events.AdminBusAssignmentRemovalEvent;
import org.tracker.ubus.ubus.Components.Buses.Bus.Events.AdminBusDeletionAuditEvent;
import org.tracker.ubus.ubus.Components.Buses.Bus.Exceptions.BusInformationMismatchException;
import org.tracker.ubus.ubus.Components.Buses.Bus.Exceptions.DuplicateDriverAssignmentException;
import org.tracker.ubus.ubus.Components.Buses.Bus.Mapper.BusMapper;
import org.tracker.ubus.ubus.Components.Buses.Bus.Repository.DatabaseAccessLayer.BusRepository;
import org.tracker.ubus.ubus.Components.Buses.Bus.Service.Interface.IBusService;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Enum.DriverSchedule;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Mappers.BusAssignmentMapper;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Repository.BusAssignmentRepository;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Entity.BusOperationalHistory;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Repository.BusOperationalHistoryRepository;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.Publisher.MultiEventPublisher;
import org.tracker.ubus.ubus.Components.Shared.Entities.BaseService;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class BusService extends BaseService implements IBusService {


    private final BusMapper busMapper;
    private final BusRepository busRepository;
    private final UserRepository userRepository;
    private final MultiEventPublisher multiEventPublisher;
    private final BusAssignmentMapper busAssignmentMapper;
    private final BusAssignmentRepository busAssignmentRepository;
    private final BusOperationalHistoryRepository busOperationalHistoryRepository;


    @Override
    @Transactional
    public BusRegisterResponse registerBus(BusRegisterRequest request) {
        var bus = this.busMapper.toEntity(request);
        this.validateBusConstraints(bus);

        var savedBus = this.busRepository.save(bus);
        var driverIds = request.driverIds();

        var busRegisterResponse = this.busMapper.toDTO(savedBus);

        //if we have no ids to work with, return the bus
        if(driverIds == null || driverIds.isEmpty())
            return busRegisterResponse;

        //save the bus assignment
        this.saveBusAssignment(driverIds, savedBus);
        return busRegisterResponse;
    }


    @Override
    public void editBusActivityStatus(UUID busId, String activityStatus) {

        var activityStatusEnum = BusActivityStatus.fromLabel(activityStatus);
        var bus = this.busRepository.findByIdOrThrow(busId);
        bus.setActivityStatus(activityStatusEnum); // save the new status
        this.busRepository.save(bus);
    }


    @Override
    @Transactional
    public void deleteBus(UUID busId) {


        var bus = this.busRepository.findByIdOrThrow(busId);
        if(!bus.isActive())
            throw new IllegalStateException("Bus is already deleted");

        this.busAssignmentRepository.deleteByBus(bus); //delete all bus assignments
        bus.setActive(false); //soft delete the bus
        this.busRepository.save(bus);

        var admin = this.getCurrentUser();
        multiEventPublisher.publish(() -> new AdminBusDeletionAuditEvent(this, admin, bus));
    }


    @Override
    public List<BusAdminViewResponse> viewBuses() {
        var busesAssignedAndNot = this.busRepository.findAllBusesWithAssignment();
        return this.busMapper.toDTOs(busesAssignedAndNot);
    }


    @Transactional
    @Override
    public void editOperationalStatus(BusEditRequest busEditRequest) {


        var bus = this.busRepository.findByIdOrThrow(busEditRequest.id());
        var operationalStatus = BusOperationalStatus.fromLabel(busEditRequest.operationalStatus());
        bus.setOperationalStatus(operationalStatus);

        BusOperationalHistory busOperationalHistory = BusOperationalHistory.builder()
                .bus(bus)
                .busOperationalStatus(operationalStatus)
                .dateOperated(LocalDate.now())
                .build();


        this.busOperationalHistoryRepository.save(busOperationalHistory);

        if(bus.getOperationalStatus() == BusOperationalStatus.OUT_OF_SERVICE) {

            var busName = bus.getName();
            var busAssignments = this.busAssignmentRepository.findByBusName(busName);


            //getting the list of drivers from the given assignment
            var drivers = busAssignments.stream()
                    .map(BusAssignment::getDriver)
                    .toList();


            this.busAssignmentRepository.deleteByBus(bus);
            this.multiEventPublisher.publish(() -> new AdminBusAssignmentRemovalEvent(this, bus, drivers));
        }
        this.busRepository.save(bus);
    }


    @Override
    @Transactional
    public void editBus(BusRegisterRequest request) {
        var bus = this.busRepository.findByIdOrThrow(request.busId());

        // Update bus fields with new values from request
        bus.setName(request.name());
        bus.setCapacity(request.capacity());
        bus.setType(BusType.fromLabel(request.type()));
        bus.setRegistrationNumber(request.registrationNumber());
        bus.setModel(request.model());
        bus.setCapacity(request.capacity());
        bus.setOperationalStatus(BusOperationalStatus.fromLabel(request.operationalStatus()));

        // Save the updated bus
        this.busRepository.save(bus);

        // Update bus assignments if driver IDs are provided
        if(request.driverIds() != null && !request.driverIds().isEmpty()) {
            // Delete existing assignments
            this.busAssignmentRepository.deleteByBusId(bus.getId());

            // Create new assignments
            this.saveBusAssignment(request.driverIds(), bus);
        }
    }


    private void validateBusConstraints(Bus bus)
            throws BusInformationMismatchException {

        var operationalStatus = bus.getOperationalStatus();;
        var activityStatus = bus.getActivityStatus();

        // Constraint: Bus in maintenance or out of service cannot be on trip
        // or be loading passengers or on break
        if ((operationalStatus == BusOperationalStatus.MAINTENANCE ||
                operationalStatus == BusOperationalStatus.OUT_OF_SERVICE) &&
                (activityStatus == BusActivityStatus.ON_TRIP ||
                        activityStatus == BusActivityStatus.BREAK ||
                        activityStatus == BusActivityStatus.LOADING_PASSENGERS)) {
            throw new BusInformationMismatchException(
                    String.format("Bus in %s cannot be %s",
                            operationalStatus.getLabel(),
                            activityStatus.getLabel())
            );
        }
    }


    private void saveBusAssignment(Collection<UUID> driverIds, Bus bus)
            throws DuplicateDriverAssignmentException {

        var drivers = this.userRepository.findByIdIn(driverIds);
        if(drivers.size() == 2) {
             var firstDriver = drivers.get(0);
             var secondDriver = drivers.get(1);
             if(firstDriver.equals(secondDriver))
                 throw new DuplicateDriverAssignmentException("Duplicate Drivers Found");
        }

        var busAssignments =new ArrayList<BusAssignment>();


        var counter = new AtomicInteger();
        var drivingSchedules = DriverSchedule.values();


        drivers.forEach(driver -> {
            var assignment = this.busAssignmentMapper.toEntity(bus, driver, drivingSchedules[counter.get()]);
            busAssignments.add(assignment);
            counter.getAndIncrement();
        });
        //save the bus and the bus assignments
        this.busRepository.save(bus);
        this.busAssignmentRepository.flush();
        this.busAssignmentRepository.saveAll(busAssignments);
    }

}


