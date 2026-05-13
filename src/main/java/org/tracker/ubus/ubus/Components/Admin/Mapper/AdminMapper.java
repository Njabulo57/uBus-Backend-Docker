package org.tracker.ubus.ubus.Components.Admin.Mapper;


import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Admin.DTO.Response.DriverPendingResponseDTO;
import org.tracker.ubus.ubus.Components.User.Entity.User;
import java.util.Collection;
import java.util.List;

@Component
public class AdminMapper {


    public Collection<DriverPendingResponseDTO> toPendingDrivers(List<User> users) {
        return users.stream()
                .map(this::toDriverPendingResponseDTO)
                .toList();
    }



    private DriverPendingResponseDTO toDriverPendingResponseDTO(User user) {
        return DriverPendingResponseDTO.builder()
                .driverId(user.getId())
                .firstName(user.getFirstname())
                .lastName(user.getLastname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
