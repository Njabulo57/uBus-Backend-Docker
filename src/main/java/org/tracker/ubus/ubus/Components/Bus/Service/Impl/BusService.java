package org.tracker.ubus.ubus.Components.Bus.Service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Requests.BusRegisterRequest;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Responses.BusAdminViewWrapperResponse;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Responses.BusRegisterResponse;
import org.tracker.ubus.ubus.Components.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Bus.Enum.BusActivityStatus;
import org.tracker.ubus.ubus.Components.Bus.Enum.BusOperationalStatus;
import org.tracker.ubus.ubus.Components.Bus.Exceptions.BusAlreadyExistsException;
import org.tracker.ubus.ubus.Components.Bus.Exceptions.BusInformationMismatchException;
import org.tracker.ubus.ubus.Components.Bus.Mapper.BusMapper;
import org.tracker.ubus.ubus.Components.Bus.Repository.BusRepository;
import org.tracker.ubus.ubus.Components.Bus.Service.Interface.IBusService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusService implements IBusService {

    private final BusMapper busMapper;
    private final BusRepository busRepository;



    @Override
    public BusRegisterResponse registerBus(BusRegisterRequest request) {

        Bus bus = this.busMapper.toEntity(request);
        this.validateBusConstraints(bus);



        boolean existsByName = busRepository.existsByName(bus.getName());
        if (existsByName)
            throw new BusAlreadyExistsException("Bus with name " + bus.getName() + " already exists.", bus);

        this.busRepository.save(bus);
        return this.busMapper.toDTO(bus);
    }

    @Override
    public BusAdminViewWrapperResponse viewBuses() {

        List<Bus> buses = this.busRepository.findAllByIsActiveTrue();

        return this.busMapper.toDTO(buses);
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


