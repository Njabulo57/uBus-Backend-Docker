package org.tracker.ubus.ubus.Components.Auth.Service.Impl;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.EmailOtpRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.LoginRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.RegisterRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.EmailAuthenticationResponse;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.LoginSuccessfulResponse;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Responses.RegisterSuccessfulResponse;
import org.tracker.ubus.ubus.Components.Auth.Exception.External.*;
import org.tracker.ubus.ubus.Components.Auth.Exception.Internal.AccountLockedException;
import org.tracker.ubus.ubus.Components.Auth.Exception.Internal.AdminRegistrationNotAllowedException;
import org.tracker.ubus.ubus.Components.Auth.Mapper.AuthMapper;
import org.tracker.ubus.ubus.Components.Auth.Service.Interface.IAuthService;
import org.tracker.ubus.ubus.Components.Auth.VerificationDispatcher.VerificationDispatcher;
import org.tracker.ubus.ubus.Components.Jwt.JwtService.JwtService;
import org.tracker.ubus.ubus.Components.TokenBlacklist.Service.Impl.BlacklistedTokenService;


import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;
import java.time.LocalDateTime;

import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole.STUDENT;
import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus.*;


@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {


    private final JwtService jwtService;
    private final AuthMapper authMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationDispatcher verificationDispatcher;
    private final BlacklistedTokenService blacklistedTokenService;


    @Override
    public LoginSuccessfulResponse login(LoginRequest loginRequest) {

        String email = loginRequest.email();
        var userOptional = this.userRepository.findByEmail(email);
        var user = userOptional.orElseThrow(() ->
                new InvalidCredentialsException("Invalid Credentials"));

        if(user.getStatus() == EMAIL_APPROVAL_PENDING ||
                user.getStatus() == ADMIN_APPROVAL_PENDING ||
                user.getStatus() == INACTIVE)
            throw new AccountLockedException("Account Locked.");


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


        UserStatus userStatus = switch (userRole) {

            case STUDENT -> {
                this.validateIfStudent(registerRequest.email(), userRole); // validate the student's information
                yield EMAIL_APPROVAL_PENDING;
            }

            case STAFF -> EMAIL_APPROVAL_PENDING;

            case DRIVER -> ADMIN_APPROVAL_PENDING;

            case ADMIN -> throw new AdminRegistrationNotAllowedException("Admin Registration Not Supported");
        };

        var userEntity = this.authMapper.toEntity(registerRequest, userRole, userStatus);


        final var encodedPassed = this.passwordEncoder.encode(userEntity.getPassword()); //hashed the password
        userEntity.setPassword(encodedPassed); //add the encoded version of the password
        var savedUser = this.userRepository.save(userEntity); //save the user.

        //send the user a verification method according to their type
        this.verificationDispatcher.dispatchRegistrationOTP(savedUser);
        return this.authMapper.toRegisterDTO(savedUser.getRole());
    }


    @Override
    public boolean verifyEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }


    @Override
    @Transactional
    public EmailAuthenticationResponse requestEmailVerification(EmailOtpRequest emailOtpRequest) {

        User user = this.userRepository.findByEmail(emailOtpRequest.email())
                .orElseThrow(() -> new AccountNotFoundException("Account Doesn't Exist"));

        switch(user.getStatus()) {
            case EMAIL_APPROVAL_PENDING -> this.verificationDispatcher.dispatchRegistrationOTP(user);
            case ACTIVE -> throw new RuntimeException("User is already verified");
            case INACTIVE -> throw new AccountLockedException("Account is disabled. Contact support.");
        }

        var now = LocalDateTime.now();
        return new EmailAuthenticationResponse("OTP generated and sent to your email", now);
    }


    @Override
    public void logout() {

        HttpServletRequest request = (HttpServletRequest) ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes()).getRequest();

        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7);
        long remainingMilliseconds = jwtService.getRemainingExpiration(token);
        blacklistedTokenService.blacklist(token, remainingMilliseconds);
    }


    /**
     * Validates if the provided email and user role correspond to a valid student.
     *
     * @param email the email address to validate. It must end with the domain "@student.uj.ac.za".
     *              The part before the domain must be a 9-digit student number.
     * @param userRole the role of the user, which must correspond to a student role for validation to pass.
     * @throws InvalidStudentInformationException if the email does not belong to the student domain,
     *                                            does not contain a valid 9-digit student number,
     *                                            or if other student-related validations fail.
     */
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

    /**
     * Validates if the student email address corresponds correctly to the provided user role.
     * Ensures that only users with the STUDENT role can use a student email, and that
     * users with the STUDENT role must provide a valid student email address.
     *
     * @param extractedStudentNumber The extracted student number from the email address.
     *                               Should be non-null for student email addresses.
     * @param studentRole            The role of the user, expected to be either STUDENT
     *                               or a non-student role.
     * @throws InvalidStudentInformationException If the role and email address are not compatible:
     *                                            - STUDENT role provided without a valid student email address.
     *                                            - Non-STUDENT role provided with a student email address.
     */
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


