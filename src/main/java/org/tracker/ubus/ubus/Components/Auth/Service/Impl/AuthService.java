package org.tracker.ubus.ubus.Components.Auth.Service.Impl;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.EmailOtpRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.LoginRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.RegisterRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.EmailOtpResponse;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.LoginSuccessfulResponse;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.RegisterSuccessfulResponse;
import org.tracker.ubus.ubus.Components.Auth.Exception.External.*;
import org.tracker.ubus.ubus.Components.Auth.Exception.Internal.AccountLockedException;
import org.tracker.ubus.ubus.Components.Auth.Exception.Internal.AdminRegistrationNotAllowedException;
import org.tracker.ubus.ubus.Components.Auth.Mapper.AuthMapper;
import org.tracker.ubus.ubus.Components.Auth.Service.Interface.IAuthService;
import org.tracker.ubus.ubus.Components.Auth.VerificationDispatcher.VerificationDispatcher;
import org.tracker.ubus.ubus.Components.TokenGenerators.EmailVerificationToken.EmailVerificationTokenService.EmailVerificationTokenService;
import org.tracker.ubus.ubus.Components.TokenGenerators.Jwt.JwtService.JwtService;
import org.tracker.ubus.ubus.Components.User.Entity.User;
import org.tracker.ubus.ubus.Components.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.User.Repository.UserRepository;
import java.time.LocalDateTime;

import static org.tracker.ubus.ubus.Components.User.Enum.UserRole.STUDENT;
import static org.tracker.ubus.ubus.Components.User.Enum.UserStatus.*;


@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {


    private final JwtService jwtService;
    private final AuthMapper authMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationTokenService emailVerificationTokenService;
    private final VerificationDispatcher verificationDispatcher;

    @Override
    public LoginSuccessfulResponse login(LoginRequest loginRequest) {

        String email = loginRequest.email();
        var userOptional = this.userRepository.findByEmail(email);
        var user = userOptional.orElseThrow(() ->
                new InvalidCredentialsException("Invalid Credentials"));

        if(user.getStatus() == EMAIL_APPROVAL_PENDING ||
                user.getStatus() == ADMIN_APPROVAL_PENDING ||
                user.getStatus() == INACTIVE)
            throw new AccountLockedException("Account is locked. Please Verify");

        if(!passwordEncoder.matches(loginRequest.password(), user.getPassword()))
            throw new InvalidCredentialsException("Invalid Credentials");

        //user found from this point
        String role = user.getRole().getLabel();
        String token = this.jwtService.generateToken(user, role);

        return authMapper.toDTO(token, role);
    }


    @Override
    @Transactional
    public RegisterSuccessfulResponse register(RegisterRequest registerRequest) {

        var userRole = UserRole.fromLabel(registerRequest.role());

        var userExists = this.userRepository.existsByEmail(registerRequest.email());
        if(userExists)
            throw new DuplicateEmailException("Email Already Exists");



        //this will create the user entity based off of their role
        var userEntity = switch (userRole) {

            case STUDENT -> {
                this.validateIfStudent(registerRequest.email(), userRole); // validate the student's information
                yield this.authMapper.toEntity(registerRequest, userRole, EMAIL_APPROVAL_PENDING);
            }

            case STAFF -> throw new AccountLockedException("Role Not Supported");

            case DRIVER -> this.authMapper.toEntity(registerRequest, userRole, ADMIN_APPROVAL_PENDING);

            case ADMIN -> throw new AdminRegistrationNotAllowedException("Admin Registration Not Supported");
        };




        final var encodedPassed = this.passwordEncoder.encode(userEntity.getPassword()); //hashed the password
        userEntity.setPassword(encodedPassed); //add the encoded version of the password
        var savedUser = this.userRepository.save(userEntity); //save the user.

        //send the user a verification method according to their type
        this.verificationDispatcher.dispatch(savedUser);
        return this.authMapper.toRegisterDTO();
    }


    @Override
    @Transactional
    public EmailOtpResponse requestEmailVerification(EmailOtpRequest emailOtpRequest) {

        User user = this.userRepository.findByEmail(emailOtpRequest.email())
                .orElseThrow(() -> new AccountNotFoundException("Account Doesn't Exist"));

        switch(user.getStatus()) {
            case EMAIL_APPROVAL_PENDING -> this.verificationDispatcher.dispatch(user);
            case ACTIVE -> throw new RuntimeException("User is already verified");
            case INACTIVE -> throw new AccountLockedException("Account is disabled. Contact support.");
        }

        var now = LocalDateTime.now();
        return new EmailOtpResponse("OTP generated and sent to your email", now);
    }


    @Override
    @Transactional
    public boolean verifyEmailToken(String token) {

        var emailToken = this.emailVerificationTokenService.getByToken(token);
        if(emailToken.isExpired())
            this.emailVerificationTokenService.deleteToken(emailToken);

        if(!token.equals(emailToken.getToken()))
            return false;
        return true;
    }



    private void validateIfStudent(String email, UserRole userRole)
            throws InvalidStudentInformationException {

        // Check if it's a student email domain
        if (!email.endsWith("@student.uj.ac.za"))
            throw new InvalidStudentInformationException("Invalid Student Email Address");

        // Extract student number
        String studentNumberPart = email.split("@student.uj.ac.za")[0];

        // Validate exactly 9 digits using regex
        if (!studentNumberPart.matches("\\d{9}"))
            throw new InvalidStudentInformationException(
                    "Invalid student number"
            );

        this.validateIfStudentEmailCorrect(studentNumberPart, userRole);
    }

    private void validateIfStudentEmailCorrect(String extractedStudentNumber, UserRole studentRole)
            throws InvalidStudentInformationException {
        boolean isStudentEmail = extractedStudentNumber != null;

        // Role and email compatibility validation
        if(studentRole == STUDENT && !isStudentEmail)
            throw new InvalidStudentInformationException(
                    "Students must register with a valid @student.uj.ac.za email address");


        if(studentRole != STUDENT && isStudentEmail)
            throw new InvalidStudentInformationException(
                    "Only students role can register with a @student.uj.ac.za email address");
    }

}


