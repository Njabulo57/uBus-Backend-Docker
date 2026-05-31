package org.tracker.ubus.ubus.Components.Bus.Service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Requests.BusEditRequest;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Requests.BusRegisterRequest;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Responses.BusAdminViewResponse;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Responses.BusRegisterResponse;
import org.tracker.ubus.ubus.Components.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Bus.Enum.BusActivityStatus;
import org.tracker.ubus.ubus.Components.Bus.Enum.BusOperationalStatus;
import org.tracker.ubus.ubus.Components.Bus.Exceptions.BusAlreadyExistsException;
import org.tracker.ubus.ubus.Components.Bus.Exceptions.BusInformationMismatchException;
import org.tracker.ubus.ubus.Components.Bus.Mapper.BusMapper;
import org.tracker.ubus.ubus.Components.Bus.Repository.DatabaseAccessLayer.BusRepository;
import org.tracker.ubus.ubus.Components.Bus.Service.Interface.IBusService;
import org.tracker.ubus.ubus.Components.BusRoute.Mappers.BusRouteMapper;
import org.tracker.ubus.ubus.Components.BusRoute.Repository.BusRouteRepository;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusService implements IBusService {


    private final BusMapper busMapper;
    private final BusRepository busRepository;
    private final BusRouteMapper busRouteMapper;
    private final BusRouteRepository busRouteRepository;




    @Override
    @Transactional
    public BusRegisterResponse registerBus(BusRegisterRequest request) {

        var route = Route.valueOf(request.toRoute());

        Bus bus = this.busMapper.toEntity(request);
        this.validateBusConstraints(bus);


        boolean existsByName = busRepository.existsByName(bus.getName());
        if (existsByName)
            throw new BusAlreadyExistsException("Bus with name already exists.", bus);

        var busRoute = this.busRouteMapper.toEntity(bus, route);

        this.busRepository.save(bus);
        this.busRouteRepository.save(busRoute);
        return this.busMapper.toDTO(bus);
    }

    @Override
    public void editBus(BusEditRequest request, UUID busId) {

        var bus = this.busRepository.findByIdOrThrow(busId);
        var editedBus = this.busMapper.editBus(request, bus);
        this.busRepository.save(editedBus);
    }

    @Override
    public List<BusAdminViewResponse> viewBuses() {
        var busesAssignedAndNot = this.busRepository.findAssignedBusesOrDefault();
        return this.busMapper.toDTOs(busesAssignedAndNot);
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

}


