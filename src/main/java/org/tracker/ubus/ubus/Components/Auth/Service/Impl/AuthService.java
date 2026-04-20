package org.tracker.ubus.ubus.Components.Auth.Service.Impl;


import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.LoginRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.LoginSuccessfulResponse;
import org.tracker.ubus.ubus.Components.Auth.Mapper.AuthMapper;
import org.tracker.ubus.ubus.Components.Auth.Service.Interface.IAuthService;

import org.tracker.ubus.ubus.Components.Jwt.JwtService.JwtService;
import org.tracker.ubus.ubus.Configuration.Security.UserPrincipal;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final JwtService jwtService;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    @Override
    public LoginSuccessfulResponse login(LoginRequest loginRequest) {

        String email = loginRequest.email();
        UserPrincipal userDetails = (UserPrincipal) this.userDetailsService.loadUserByUsername(email);

        if(!passwordEncoder.matches(loginRequest.password(), userDetails.getPassword()))
            throw new BadCredentialsException("Incorrect user credentials");

        //user found from this point
        String token = this.jwtService.generateToken(userDetails);
        String role = userDetails.getRole();

        return authMapper.toDTO(token, role);
    }




}


