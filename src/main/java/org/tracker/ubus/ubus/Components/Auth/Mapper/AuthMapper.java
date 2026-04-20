package org.tracker.ubus.ubus.Components.Auth.Mapper;


import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.LoginSuccessfulResponse;

import java.time.LocalDateTime;

@Component
public class AuthMapper {


    public LoginSuccessfulResponse toDTO(String token, String role) {

        LocalDateTime createdAt = LocalDateTime.now();
        return LoginSuccessfulResponse.builder()
                .token(token)
                .role(role)
                .createdAt(createdAt)
                .build();
    }
}
