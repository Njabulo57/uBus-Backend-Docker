package org.tracker.ubus.ubus.Components.OneTimePassword.Service.Impl;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Requests.OtpValidationRequest;
import org.tracker.ubus.ubus.Components.OneTimePassword.Entity.OneTimePassword;
import org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions.OneTimePasswordExpiredException;
import org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions.OneTimePasswordMismatchException;
import org.tracker.ubus.ubus.Components.OneTimePassword.Generator.OneTimePasswordGenerator;
import org.tracker.ubus.ubus.Components.OneTimePassword.Reposirtory.OneTimePasswordRepository;
import org.tracker.ubus.ubus.Components.OneTimePassword.Service.Interface.IOneTimePasswordService;
import org.tracker.ubus.ubus.Components.User.Enum.UserStatus;
import org.tracker.ubus.ubus.Components.User.Repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OneTimePasswordService implements IOneTimePasswordService {

    @Value("${otp.duration}")
    private Integer duration;
    private final UserRepository userRepository;
    private final PasswordEncoder bcryptEncoder;
    private final OneTimePasswordGenerator oneTimePasswordGenerator;
    private final OneTimePasswordRepository oneTimePasswordRepository;



    /**
     * generates a one time password for a user
     * @param userId user to give a opt
     * @return returns the generated otp
     * @throws OneTimePasswordMismatchException if the otp already exists and is still valid
     */
    @Override
    public String generateOTP(UUID userId) {


        var exists = this.oneTimePasswordRepository.existsByUser(userId);

        if (exists) {
            OneTimePassword oneTimePassword = this.oneTimePasswordRepository.findByUserOrThrow(userId);

            if(oneTimePassword.isExpired()) {
                //get the time between now and the expiration time in minutes
                long expiresIn = Duration.between(LocalDateTime.now(), oneTimePassword.getExpiresAt()).toMinutes();
                throw new OneTimePasswordMismatchException("Valid OPT already exists.Please Use It Before It Expires", expiresIn);
            }else {
                this.oneTimePasswordRepository.delete(oneTimePassword); //delete the expired one
            }
        }

        var user = this.userRepository.findByIdOrThrow(userId);//getting the user from the db

        final var generateOTP = oneTimePasswordGenerator.generateOTP(); // generating the otp
        final var hashedOPT = bcryptEncoder.encode(generateOTP); //hash the password
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(duration);

        var otp = OneTimePassword.builder()
                .otp(hashedOPT)
                .expiresAt(expiresAt)
                .user(user)
                .build();

        this.oneTimePasswordRepository.save(otp); //save the entry

        return generateOTP;
    }


    /**
     * this validates the OPT. it will either succeed or throw an exception
     * @param otpValidationRequest object containing the otp code and the user it
     * belongs to
     * @return returns true if it succeeds
     */
    @Transactional
    public boolean validateOTP(OtpValidationRequest otpValidationRequest)
            throws OneTimePasswordExpiredException, OneTimePasswordMismatchException {

        // Find OTP by user ID
        var oneTimePassword = this.oneTimePasswordRepository.findByUserOrThrow(otpValidationRequest.userId());

        if(oneTimePassword.isExpired())
            throw new OneTimePasswordExpiredException("OTP has expired.Please Request for a new one-time password");

        // Check if OTP matches
        String dbOPT = oneTimePassword.getOtp();

        if (!bcryptEncoder.matches(otpValidationRequest.otp(), dbOPT)) //check the opts against each other's hashes
            throw new OneTimePasswordMismatchException("Invalid One time password");


        var user = oneTimePassword.getUser();
        // Update user status to ACTIVE
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);


        // Delete the used OTP
        this.oneTimePasswordRepository.delete(oneTimePassword);



        return true;
    }

}
