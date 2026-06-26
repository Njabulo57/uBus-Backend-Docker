package org.tracker.ubus.ubus.Components.Buses.Bus.Mapper;

import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.Bus.DTOs.Requests.BusEditRequest;
import org.tracker.ubus.ubus.Components.Buses.Bus.DTOs.Requests.BusRegisterRequest;
import org.tracker.ubus.ubus.Components.Buses.Bus.DTOs.Responses.BusAdminViewResponse;
import org.tracker.ubus.ubus.Components.Buses.Bus.DTOs.Responses.BusRegisterResponse;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusActivityStatus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusOperationalStatus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusType;
import org.tracker.ubus.ubus.Components.Buses.Bus.Repository.Projections.DriverWithOrWithoutAssignmentView;

import java.util.List;


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

    public Bus editBus(BusEditRequest request, Bus bus) {

        var type = BusType.fromLabel(request.type());
        var operationalStatus = BusOperationalStatus.fromLabel(request.operationalStatus());
        var activityStatus = BusActivityStatus.fromLabel(request.activityStatus());

        bus.setName(request.name());
        bus.setCapacity(request.capacity());
        bus.setType(type);
        bus.setOperationalStatus(operationalStatus);
        bus.setActivityStatus(activityStatus);
        bus.setCapacity(bus.getCapacity());

        return bus;
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

    public List<BusAdminViewResponse> toDTOs(List<DriverWithOrWithoutAssignmentView> views) {

        return views.stream()
                .map(this::toDTO)
                .toList();
    }


    private BusAdminViewResponse toDTO(DriverWithOrWithoutAssignmentView view) {

        var bus = view.getBus();
        var type = bus.getType().getLabel();
        var operationalStatus = bus.getOperationalStatus().getLabel();
        var activityStatus = bus.getActivityStatus().getLabel();

        var driver = view.getDriver();
        String currentDriver = driver != null
                ? Character.toUpperCase(driver.getFirstname().charAt(0)) + ".  " + driver.getLastname()
                : "Unassigned";

        return BusAdminViewResponse.builder()
                .id(bus.getId())
                .busName(bus.getName())
                .model(bus.getModel())
                .capacity(bus.getCapacity())
                .type(type)
                .registrationNumber(bus.getRegistrationNumber())
                .operationalStatus(operationalStatus)
                .activityStatus(activityStatus)
                .currentDriver(currentDriver)
                .isActive(bus.isActive())
                .createdAt(bus.getCreatedAt())
                .updatedAt(bus.getUpdatedAt())
                .build();
    }


}
