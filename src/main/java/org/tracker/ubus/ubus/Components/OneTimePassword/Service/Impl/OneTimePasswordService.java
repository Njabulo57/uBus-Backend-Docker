package org.tracker.ubus.ubus.Components.OneTimePassword.Service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Internal.OtpInternalCarrier;
import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Requests.OtpValidationRequest;
import org.tracker.ubus.ubus.Components.OneTimePassword.Entity.OneTimePassword;
import org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions.OneTimePasswordExpiredException;
import org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions.OneTimePasswordExistsException;
import org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions.OneTimePasswordMismatchException;
import org.tracker.ubus.ubus.Components.OneTimePassword.Generator.OneTimePasswordGenerator;
import org.tracker.ubus.ubus.Components.OneTimePassword.Reposirtory.OneTimePasswordRepository;
import org.tracker.ubus.ubus.Components.OneTimePassword.Service.Interface.IOneTimePasswordService;
import org.tracker.ubus.ubus.Components.User.Enum.UserStatus;
import org.tracker.ubus.ubus.Components.User.Repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OneTimePasswordService implements IOneTimePasswordService {

    @Value("${otp.durationDurationMinutes}")
    private int expiryDuration;
    private final UserRepository userRepository;
    private final OneTimePasswordGenerator oneTimePasswordGenerator;
    private final OneTimePasswordRepository oneTimePasswordRepository;



    /**
     * generates a one time password for a user
     * @param userId user to give a opt
     * @return returns the generated otp
     * @throws OneTimePasswordExistsException if the otp already exists and is still valid
     */
    @Override
    public OtpInternalCarrier generateOTP(UUID userId) throws
            OneTimePasswordExistsException {

        var exists = this.oneTimePasswordRepository.existsByUser(userId);

        if (exists) {
            OneTimePassword oneTimePassword = this.oneTimePasswordRepository.findByUserOrThrow(userId);

            if(!oneTimePassword.isExpired()) {
                //get the time between now and the expiration time in minutes
                long expiresIn = Duration.between(LocalDateTime.now(), oneTimePassword.getExpiresAt()).toMinutes();
                throw new OneTimePasswordExistsException("Valid OPT already exists.Please Use It Before It Expires", expiresIn);
            }else
                this.oneTimePasswordRepository.delete(oneTimePassword); //delete the expired one

        }

        var user = this.userRepository.findByIdOrThrow(userId);//getting the user from the db
        var generateOTP = oneTimePasswordGenerator.generateOTP(); // generating the otp

        generateOTP = this.validateIfOtpUnique(generateOTP);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expiryDuration);

        var otp = OneTimePassword.builder()
                .otp(generateOTP)
                .expiresAt(expiresAt)
                .user(user)
                .build();

        this.oneTimePasswordRepository.save(otp); //save the entry
        return new OtpInternalCarrier(generateOTP, expiryDuration);
    }


    /**
     * this validates the OPT. it will either succeed or throw an exception
     * @param otpValidationRequest object containing the otp code and the user it
     * belongs to
     * @return returns true if it succeeds
     */
    @Transactional
    public boolean validateOTP(OtpValidationRequest otpValidationRequest)
            throws OneTimePasswordExpiredException, OneTimePasswordExistsException {


        var oneTimePasswordOptional = this.oneTimePasswordRepository.findByOtp(otpValidationRequest.otp());
        var oneTimePassword = oneTimePasswordOptional
                .orElseThrow(() -> new OneTimePasswordMismatchException("Invalid Otp"));

        if(oneTimePassword.isExpired())
            throw new OneTimePasswordExpiredException("OTP has expired.Please Request for a new one time password");


        if(!Objects.equals(oneTimePassword.getOtp(), otpValidationRequest.otp()))
            throw new OneTimePasswordMismatchException("Invalid Opt");

        var user = oneTimePassword.getUser();
        // Update user status to ACTIVE
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        // Delete the used OTP
        this.oneTimePasswordRepository.delete(oneTimePassword);

        return true;
    }



    @Scheduled(cron = "0 0 */5 * * *")
    protected void removeUnusedOTPs() {
        var unusedOTPs = this.oneTimePasswordRepository.findAllOTPs();

        List<OneTimePassword> expiredOTPs = unusedOTPs.stream()
                .filter(OneTimePassword::isExpired)
                .toList();

        List<OneTimePassword> nonExpiredOTPs = unusedOTPs.stream()
                .filter(oneTimePassword -> !oneTimePassword.isExpired())
                .toList();

        if (!expiredOTPs.isEmpty()) {
            this.oneTimePasswordRepository.deleteAll(expiredOTPs);
            log.info("Deleted {} expired OTPs", expiredOTPs.size());
        }else
            log.info("No expired OTPs detected. Found {} valid OTPs", nonExpiredOTPs.size());

    }


    private String validateIfOtpUnique(String generatedOtp) {

        while(this.oneTimePasswordRepository.existsByOtp(generatedOtp))
            generatedOtp = this.oneTimePasswordGenerator.generateOTP();

        return generatedOtp;
    }

}
