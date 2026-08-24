package org.tracker.ubus.ubus.Components.Users.Admin.Mapper;


import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Repository.Projections.DriverWithOrWithoutAssignmentView;
import org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response.DriverActiveResponseDTO;
import org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response.DriverBusResponse;
import org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response.DriverPendingResponseDTO;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Component
public class AdminMapper {


    public Collection<DriverPendingResponseDTO> toPendingDrivers(List<User> users) {
        return users.stream()
                .map(this::toDriverPendingResponseDTO)
                .toList();
    }


    public Collection<DriverActiveResponseDTO> toDTOs(Collection<DriverWithOrWithoutAssignmentView> views) {
        return views.stream()
                .map(this::toActiveDriverResponseDTO)
                .sorted(Comparator.comparing(DriverActiveResponseDTO::lastName))
                .toList();
    }



    public Collection<DriverActiveResponseDTO> toActiveDrivers(List<User> users) {
        return users.stream()
                .map(this::toActiveDriverResponseDTO)
                .sorted(Comparator.comparing(DriverActiveResponseDTO::lastName))
                .toList();
    }

    private DriverActiveResponseDTO toActiveDriverResponseDTO(DriverWithOrWithoutAssignmentView view) {

        var driver = view.getDriver();
        return DriverActiveResponseDTO.builder()
                .driverId(driver.getId())
                .firstName(driver.getFirstname())
                .lastName(driver.getLastname())
                .email(driver.getEmail())
                .phoneNumber(driver.getPhoneNumber())
                .isAssigned(view.getAssigned())
                .busAssignedTo(view.getBus() != null ? toDTO(view.getBus()) : null)
                .build();
    }



    private DriverBusResponse toDTO(Bus bus) {

        var destinationsEnum = bus.getRoute() != null ? bus.getRoute().getDestinations() : null;
        String[] destinationArray = null;

        if(destinationsEnum != null) {
            destinationArray = destinationsEnum.stream()
                    .map(Enum::name)
                    .toArray(String[]::new);
        }
        return DriverBusResponse.builder()
                .busName(bus.getName())
                .destinations(destinationArray)
                .build();
    }

    private DriverActiveResponseDTO toActiveDriverResponseDTO(User user) {

        var phoneNumber = this.formatPhoneNumber(user.getPhoneNumber());
        return DriverActiveResponseDTO.builder()
                .driverId(user.getId())
                .firstName(user.getFirstname())
                .lastName(user.getLastname())
                .email(user.getEmail())
                .phoneNumber(phoneNumber)
                .build();
    }

    private DriverPendingResponseDTO toDriverPendingResponseDTO(User user) {

        var phoneNumber = this.formatPhoneNumber(user.getPhoneNumber());
        return DriverPendingResponseDTO.builder()
                .driverId(user.getId())
                .firstName(user.getFirstname())
                .lastName(user.getLastname())
                .email(user.getEmail())
                .phoneNumber(phoneNumber)
                .build();
    }


    private String formatPhoneNumber(String phoneNumber) {

        if(phoneNumber ==null ||  phoneNumber.isEmpty())
            return "No Phone Number"

                    ;
        // Remove any non-digit characters except '+'
        String cleaned = phoneNumber.replaceAll("[^\\d+]", "");

        // Check for South African format (+27 followed by 9 digits)
        if (cleaned.startsWith("+27") && cleaned.length() == 12) {
            // Format as +27-X-XXX-XXXX
            return "+27" + "-" +
                    cleaned.substring(3, 4) + "-" +
                    cleaned.substring(4, 7) + "-" +
                    cleaned.substring(7, 11);
        }

        // Check for standard 10-digit format
        String digits = cleaned.replaceAll("\\D", "");
        if (digits.length() == 10) {
            // Format as XXX-XXX-XXXX
            return digits.substring(0, 3) + "-" +
                    digits.substring(3, 6) + "-" +
                    digits.substring(6, 10);
        }

        // Return original if format doesn't match
        return phoneNumber;
    }

}
