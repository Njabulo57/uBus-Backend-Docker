package org.tracker.ubus.ubus.Components.OneTimePassword.Service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Audit.Service.TokenCredentialService;
import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Internal.OtpInternalCarrier;
import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Requests.OtpValidationRequest;
import org.tracker.ubus.ubus.Components.OneTimePassword.Entity.OneTimePassword;
import org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions.OneTimePasswordExpiredException;
import org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions.OneTimePasswordExistsException;
import org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions.OneTimePasswordMismatchException;
import org.tracker.ubus.ubus.Components.OneTimePassword.Generator.OneTimePasswordGenerator;
import org.tracker.ubus.ubus.Components.OneTimePassword.Reposirtory.OneTimePasswordRepository;
import org.tracker.ubus.ubus.Components.OneTimePassword.Service.Interface.IOneTimePasswordService;
import org.tracker.ubus.ubus.Components.Users.PendingAdmin.Entity.PendingAdmin;
import org.tracker.ubus.ubus.Components.Users.PendingAdmin.PendingAdminRepository.PendingAdminRepository;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class OneTimePasswordService extends TokenCredentialService implements IOneTimePasswordService {

    @Value("${otp.durationDurationMinutes}")
    private int expiryDuration;

    @Value("${otp.admin.durationDurationMinutes:20}")
    private int adminExpiryDuration;


    private final UserRepository userRepository;
    private final PendingAdminRepository pendingAdminRepository;
    private final OneTimePasswordGenerator oneTimePasswordGenerator;
    private final OneTimePasswordRepository oneTimePasswordRepository;



    @Override
    public OtpInternalCarrier generateAuthToken(UUID userId) {

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
        return generateOTP(user);
    }



    @Override
    @Transactional
    public OtpInternalCarrier generateAuthToken(String email) {

        var exists = this.oneTimePasswordRepository.existsByAdminEmail(email);

        if (exists) {
            OneTimePassword oneTimePassword = this.oneTimePasswordRepository.findByPendingAdmin(email);
            if(!oneTimePassword.isExpired()) {

                //get the time between now and the expiration time in minutes
                long expiresIn = Duration.between(LocalDateTime.now(), oneTimePassword.getExpiresAt()).toMinutes();
                throw new OneTimePasswordExistsException("Valid OPT already exists.Please Use It Before It Expires", expiresIn);
            }else {
                this.oneTimePasswordRepository.delete(oneTimePassword); //delete the expired one
                this.oneTimePasswordRepository.flush();
            }
        }


        var pendingAdmin = this.pendingAdminRepository.findByEmailOrThrow(email);
        return generateOTP(pendingAdmin);
    }



    @Transactional
    public boolean validateOTP(OtpValidationRequest otpValidationRequest)
            throws OneTimePasswordExpiredException, OneTimePasswordMismatchException {


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



    @Override
    @Scheduled(cron = "0 0 */5 * * *")
    protected void removedExpiredCredentials() {
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


    @Override
    protected String verifyTokenUniqueness(String token, UserRole userRole) {

        while(this.oneTimePasswordRepository.existsByOtp(token))
            token = this.oneTimePasswordGenerator.generateOTP(userRole);
        return token;
    }




    /**
     * Generates a new one-time password (OTP) for the specified user.
     * The OTP is linked to the user, stored in the database, and has an expiration time.
     *
     * @param user The user for whom the OTP is to be generated. This must be a valid User entity.
     * @return An {@link OtpInternalCarrier} object containing the generated OTP and its expiration duration.
     */
    @NonNull
    private OtpInternalCarrier generateOTP(User user) {


        var generateOTP = oneTimePasswordGenerator.generateOTP(user.getRole()); // generating the otp

        generateOTP = this.verifyTokenUniqueness(generateOTP, user.getRole());
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expiryDuration);



        var otp = OneTimePassword.builder()
                .otp(generateOTP)
                .user(user)
                .expiresAt(expiresAt)
                .build();

        this.oneTimePasswordRepository.save(otp); //save the entry
        return new OtpInternalCarrier(generateOTP, expiryDuration);
    }



    @NonNull
    private OtpInternalCarrier generateOTP(PendingAdmin pendingAdmin) {

        var generateOTP = oneTimePasswordGenerator.generateOTP(UserRole.ADMIN);

        generateOTP = this.verifyTokenUniqueness(generateOTP, UserRole.ADMIN);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(adminExpiryDuration);


        var otp = OneTimePassword.builder()
                .otp(generateOTP)
                .user(null)
                .expiresAt(expiresAt)
                .adminEmail(pendingAdmin.getEmail())
                .build();

        this.oneTimePasswordRepository.save(otp); //save the entry
        return new OtpInternalCarrier(generateOTP, expiryDuration);

    }


}
