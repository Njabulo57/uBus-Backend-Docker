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
import org.tracker.ubus.ubus.Components.Auth.Events.OtpEmailVerificationEvent;
import org.tracker.ubus.ubus.Components.Auth.Exception.*;
import org.tracker.ubus.ubus.Components.Auth.Mapper.AuthMapper;
import org.tracker.ubus.ubus.Components.Auth.Service.Interface.IAuthService;
import org.tracker.ubus.ubus.Components.EventHandler.Publisher.MultiEvenPublisher;
import org.tracker.ubus.ubus.Components.Jwt.JwtService.JwtService;
import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Internal.OtpInternalCarrier;
import org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions.OneTimePasswordExistsException;
import org.tracker.ubus.ubus.Components.OneTimePassword.Reposirtory.OneTimePasswordRepository;
import org.tracker.ubus.ubus.Components.OneTimePassword.Service.Impl.OneTimePasswordService;
import org.tracker.ubus.ubus.Components.User.Entity.User;
import org.tracker.ubus.ubus.Components.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.User.Enum.UserStatus;
import org.tracker.ubus.ubus.Components.User.Repository.UserRepository;

import java.time.LocalDateTime;

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
        var userOptional = this.userRepository.findByEmail(email);
        var user = userOptional.orElseThrow(() ->
                new InvalidCredentialsException("Invalid Credentials"));

        if(user.getStatus() == UserStatus.EMAIL_APPROVAL_PENDING)
            throw new AccountLockedException("Account is locked. Please Verify using OTP");

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

        // Validate student email and extract student number if applicable
        String extractedStudentNumber = this.validateIfStudent(registerRequest.email());
        this.validateIfStudentEmailCorrect(extractedStudentNumber, userRole);


        boolean userExists = this.userRepository.existsByEmail(registerRequest.email());
        if(userExists)
            throw new DuplicateEmailException("Email Already Exists");

        final String encodedPassed = this.passwordEncoder.encode(registerRequest.password());

        User newUser = this.authMapper.toEntity(registerRequest, extractedStudentNumber, userRole); //turn the user dto to an entity
        newUser.setPassword(encodedPassed); //add the encoded version of the password
        User savedUser = this.userRepository.save(newUser); //save the user.

        var generatedOTPCarrier = this.oneTimePasswordService.generateOTP(savedUser.getId());

        //publish this event to run else where
        publisher.publish(() ->
                new OtpEmailVerificationEvent(this, savedUser, generatedOTPCarrier ));


        var registeredMessage = "Register Successful.OTP sent for verification ";
        return this.authMapper.toRegisterDTO(registeredMessage, newUser.getEmail());
    }


    @Override
    @Transactional
    public EmailOtpResponse requestOtp(EmailOtpRequest emailOtpRequest) {


        User user = this.userRepository.findByEmail(emailOtpRequest.email())
                .orElseThrow(() -> new AccountNotFoundException("Account Doesn't Exist"));

        if(user.getStatus() != UserStatus.EMAIL_APPROVAL_PENDING)
            throw new RuntimeException("User Is Already approved");


        LocalDateTime now = LocalDateTime.now();
        OtpInternalCarrier generatedOTPCarrier;
        try {
            // This will either generate a new OTP or throw exception if valid one exists
            generatedOTPCarrier = this.oneTimePasswordService.generateOTP(user.getId());
        } catch (OneTimePasswordExistsException e) {
            //if we reach this point then the otp exists and has not expired
            return new EmailOtpResponse(e.getMessage(),now);
        }
        //we then send that otp code to the user again via email
        publisher.publish(new OtpEmailVerificationEvent(this, user, generatedOTPCarrier));
        return new EmailOtpResponse("OTP generated and sent to your email", now);
    }

    private String validateIfStudent(String email) throws InvalidStudentInformationException {

        // Check if it's a student email domain
        if (!email.endsWith("@student.uj.ac.za"))
            return null;

        // Extract student number
        String studentNumberPart = email.split("@student.uj.ac.za")[0];

        // Validate exactly 9 digits using regex
        if (!studentNumberPart.matches("\\d{9}"))
            throw new InvalidStudentInformationException(
                    "Invalid student number"
            );


        return studentNumberPart;
    }

    private void validateIfStudentEmailCorrect(String extractedStudentNumber, UserRole studentRole)
            throws InvalidStudentInformationException {
        boolean isStudentEmail = extractedStudentNumber != null;

        // Role and email compatibility validation
        if(studentRole == UserRole.STUDENT && !isStudentEmail)
            throw new InvalidStudentInformationException(
                    "Students must register with a valid @student.uj.ac.za email address");


        if(studentRole != UserRole.STUDENT && isStudentEmail)
            throw new InvalidStudentInformationException(
                    "Only students role can register with a @student.uj.ac.za email address");
    }

}


