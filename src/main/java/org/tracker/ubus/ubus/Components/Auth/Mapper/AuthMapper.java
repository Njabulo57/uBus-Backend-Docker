package org.tracker.ubus.ubus.Components.Auth.Mapper;


import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.RegisterRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.LoginSuccessfulResponse;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.RegisterSuccessfulResponse;
import org.tracker.ubus.ubus.Components.User.Entity.User;
import org.tracker.ubus.ubus.Components.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.User.Enum.UserStatus;

import java.time.LocalDateTime;

@Component
public final class AuthMapper {

    public LoginSuccessfulResponse toDTO(String token, String role) {

        LocalDateTime createdAt = LocalDateTime.now();
        return LoginSuccessfulResponse.builder()
                .token(token)
                .role(role)
                .createdAt(createdAt)
                .build();
    }

    public RegisterSuccessfulResponse toRegisterDTO(String message, String  email) {

        final LocalDateTime nowed = LocalDateTime.now();
        return RegisterSuccessfulResponse.builder()
                .otpMessage(message)
                .createdAt(nowed)
                .build();

    }


    public User toEntity(RegisterRequest registerRequest, String studentNumber, UserRole userRole) {
        return User.builder()
                .firstname(registerRequest.firstName())
                .lastname(registerRequest.lastName())
                .studentNumber(studentNumber)
                .email(registerRequest.email())
                .role(userRole)
                .status(UserStatus.EMAIL_APPROVAL_PENDING)
                .build();
    }

 }
