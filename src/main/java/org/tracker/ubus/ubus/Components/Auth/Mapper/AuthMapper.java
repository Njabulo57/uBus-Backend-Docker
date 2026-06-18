package org.tracker.ubus.ubus.Components.Auth.Mapper;


import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.RegisterRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.LoginSuccessfulResponse;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.RegisterSuccessfulResponse;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus;

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

    public RegisterSuccessfulResponse toRegisterDTO(UserRole userRole) {

        var message = "Register Successful.OTP sent for verification";
        final LocalDateTime nowed = LocalDateTime.now();
        return RegisterSuccessfulResponse.builder()
                .role(userRole.getLabel())
                .otpMessage(message)
                .createdAt(nowed)
                .build();

    }


    public User toEntity(RegisterRequest registerRequest,  UserRole userRole) {
        return User.builder()
                .firstname(registerRequest.firstName())
                .lastname(registerRequest.lastName())
                .password(registerRequest.password())
                .email(registerRequest.email())
                .role(userRole)
                .status(UserStatus.EMAIL_APPROVAL_PENDING)
                .build();
    }


    public User toEntity(RegisterRequest registerRequest,  UserRole userRole, UserStatus userStatus) {
        return User.builder()
                .firstname(registerRequest.firstName())
                .lastname(registerRequest.lastName())
                .password(registerRequest.password())
                .email(registerRequest.email())
                .role(userRole)
                .status(userStatus)
                .build();
    }

 }
