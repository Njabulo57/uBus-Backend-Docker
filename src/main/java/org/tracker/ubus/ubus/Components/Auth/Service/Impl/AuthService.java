package org.tracker.ubus.ubus.Components.Auth.Service.Impl;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.LoginRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.RegisterRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.LoginSuccessfulResponse;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.RegisterSuccessfulResponse;
import org.tracker.ubus.ubus.Components.Auth.Events.OtpEmailVerificationEvent;
import org.tracker.ubus.ubus.Components.Auth.Exception.AccountLockedException;
import org.tracker.ubus.ubus.Components.Auth.Exception.DuplicateEmailException;
import org.tracker.ubus.ubus.Components.Auth.Exception.InvalidCredentialsException;
import org.tracker.ubus.ubus.Components.Auth.Mapper.AuthMapper;
import org.tracker.ubus.ubus.Components.Auth.Service.Interface.IAuthService;
import org.tracker.ubus.ubus.Components.EventHandler.Publisher.MultiEvenPublisher;
import org.tracker.ubus.ubus.Components.Jwt.JwtService.JwtService;
import org.tracker.ubus.ubus.Components.OneTimePassword.Service.Impl.OneTimePasswordService;
import org.tracker.ubus.ubus.Components.User.Entity.User;
import org.tracker.ubus.ubus.Components.User.Enum.UserStatus;
import org.tracker.ubus.ubus.Components.User.Repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {


    private final JwtService jwtService;
    private final AuthMapper authMapper;
    private final MultiEvenPublisher publisher;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OneTimePasswordService oneTimePasswordService;

    @Override
    public LoginSuccessfulResponse login(LoginRequest loginRequest) {

        String email = loginRequest.email();
        var user = this.userRepository.findByEmailOrThrow(email);

        if(user.getStatus() == UserStatus.EMAIL_APPROVAL_PENDING)
            throw new AccountLockedException("Account is locked. Please Verify using OTP", user.getId());


        if(!passwordEncoder.matches(loginRequest.password(), user.getPassword()))
            throw new InvalidCredentialsException("Invalid Credentials");

        //user found from this point
        String role = user.getRole().toString();
        String token = this.jwtService.generateToken(user);

        return authMapper.toDTO(token, role);
    }


    @Override
    @Transactional
    public RegisterSuccessfulResponse register(RegisterRequest registerRequest) {

        boolean userExists = this.userRepository.existsByEmail(registerRequest.email());
        if(userExists)
            throw new DuplicateEmailException("Email Already Exists");

        final String encodedPassed = this.passwordEncoder.encode(registerRequest.password());

        User newUser = this.authMapper.toEntity(registerRequest); //turn the user dto to an entity
        newUser.setPassword(encodedPassed); //add the encoded version of the password
        User savedUser = this.userRepository.save(newUser); //save the user.

        var generatedStringOTP = this.oneTimePasswordService.generateOTP(savedUser.getId());

        //publish this event to run else where
        publisher.publish(() -> new OtpEmailVerificationEvent(this, generatedStringOTP, savedUser));


        var registeredMessage = "Register Successful.OTP sent for verification ";
        return this.authMapper.toDTO(registeredMessage, newUser.getId());
    }
}


