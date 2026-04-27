package org.tracker.ubus.ubus.Components.Bus.Mapper;

import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Requests.BusRegisterRequest;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Responses.BusAdminViewResponse;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Responses.BusAdminViewWrapperResponse;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Responses.BusRegisterResponse;
import org.tracker.ubus.ubus.Components.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Bus.Enum.BusActivityStatus;
import org.tracker.ubus.ubus.Components.Bus.Enum.BusOperationalStatus;
import org.tracker.ubus.ubus.Components.Bus.Enum.BusType;

import java.util.List;
import java.util.function.Predicate;

@Component
public class BusMapper {


    public Bus toEntity(BusRegisterRequest request) {

        BusType busType = BusType.fromLabel(request.type());
        BusOperationalStatus busOperationalStatus = BusOperationalStatus.fromLabel(request.operationalStatus());
        BusActivityStatus busActivityStatus = BusActivityStatus.fromLabel(request.activityStatus());
        return Bus.builder()
                .name(request.name())
                .model(request.model())
                .capacity(request.capacity())
                .type(busType)
                .operationalStatus(busOperationalStatus)
                .activityStatus(busActivityStatus)
                .isActive(true)
                .build();

    }

    public BusRegisterResponse toDTO(Bus bus) {

        var busType = bus.getType().getLabel();
        var operationalStatus = bus.getOperationalStatus().getLabel();
        var activityStatus = bus.getActivityStatus().getLabel();

        return BusRegisterResponse.builder()
                .name(bus.getName())
                .model(bus.getModel())
                .type(busType)
                .operationalStatus(operationalStatus)
                .activityStatus(activityStatus)
                .capacity(bus.getCapacity())
                .createdAt(bus.getCreatedAt())
                .build();
    }


    public BusAdminViewWrapperResponse toDTO(List<Bus> buses) {

        var busDTOs = toDTOs(buses);
        var totalOperational = this.getSpecificBusTotal(buses,
                bus -> bus.getOperationalStatus() == BusOperationalStatus.OPERATIONAL);

        var totalMaintenance = this.getSpecificBusTotal(buses,
                bus -> bus.getOperationalStatus() == BusOperationalStatus.MAINTENANCE);

        var totalOutOfService = this.getSpecificBusTotal(buses,
                bus -> bus.getOperationalStatus() == BusOperationalStatus.OUT_OF_SERVICE);


        var totalCombustion = this.getSpecificBusTotal(buses,
                bus -> bus.getType() == BusType.COMBUSTION);

        var totalElectricity = this.getSpecificBusTotal(buses,
                bus -> bus.getType() == BusType.ELECTRIC);

        //if the bus is doing anything other than being
        var totalOnDuty = this.getSpecificBusTotal(buses,
                bus -> bus.getActivityStatus() != BusActivityStatus.BREAK
        && bus.getOperationalStatus() == BusOperationalStatus.OPERATIONAL);



        return BusAdminViewWrapperResponse.builder()
                .totalBuses(buses.size())

                .totalOnDuty(totalOnDuty)

                .totalCombustion(totalCombustion)
                .totalElectric(totalElectricity)

                .totalOperational(totalOperational)
                .totalMaintenance(totalMaintenance)
                .totalOutOfService(totalOutOfService)

                .buses(busDTOs)
                .build();
    }


    private List<BusAdminViewResponse> toDTOs(List<Bus> buses) {

        return buses.stream()
                .map(bus -> {

                    var busType = bus.getType().getLabel();
                    var busOperationalStatus = bus.getOperationalStatus().getLabel();
                    var activityStatus = bus.getActivityStatus().getLabel();
                    return BusAdminViewResponse.builder()
                            .name(bus.getName())
                            .model(bus.getModel())
                            .type(busType)
                            .operationalStatus(busOperationalStatus)
                            .activityStatus(activityStatus)
                            .capacity(bus.getCapacity())
                            .createdAt(bus.getCreatedAt())
                            .updatedAt(bus.getUpdatedAt())
                            .build();
                })
                .toList();
    }

    private int getSpecificBusTotal(List<Bus> buses, Predicate<Bus> condition) {
        return (int) buses.stream()
                .filter(condition)
                .count();
    }

}
