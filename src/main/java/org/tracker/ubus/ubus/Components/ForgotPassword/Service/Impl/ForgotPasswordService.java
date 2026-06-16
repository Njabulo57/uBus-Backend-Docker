package org.tracker.ubus.ubus.Components.ForgotPassword.Service.Impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Audit.Service.TokenCredentialService;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.ChangePasswordRequest;
import org.tracker.ubus.ubus.Components.ForgotPassword.DTO.Request.ResetPasswordRequest;
import org.tracker.ubus.ubus.Components.ForgotPassword.Entity.ForgotPassword;
import org.tracker.ubus.ubus.Components.ForgotPassword.Exceptions.External.ForgotPasswordExistsException;
import org.tracker.ubus.ubus.Components.ForgotPassword.Repository.ForgotPasswordRepository;
import org.tracker.ubus.ubus.Components.ForgotPassword.Service.Interface.IForgotPasswordService;
import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Internal.OtpInternalCarrier;
import org.tracker.ubus.ubus.Components.OneTimePassword.Generator.OneTimePasswordGenerator;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;
import java.time.LocalDateTime;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class ForgotPasswordService extends TokenCredentialService implements IForgotPasswordService {

    @Value("${otp.durationDurationMinutes}")
    private int expiryDuration;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final OneTimePasswordGenerator oneTimePasswordGenerator;


    @Override
    @Transactional
    public OtpInternalCarrier generateAuthToken(UUID userId) {

        var exists = this.forgotPasswordRepository.existsByUserId(userId);

        if (exists) {
            var forgotPassword = this.forgotPasswordRepository.findByUserOrThrow(userId);

            if(!forgotPassword.isExpired())
                throw new ForgotPasswordExistsException("Valid OPT already exists.Please Use It Before It Expires");
            else
                this.forgotPasswordRepository.delete(forgotPassword); //delete the expired one

        }

        var user = this.userRepository.findByIdOrThrow(userId);//getting the user from the db
        var generateOTP = oneTimePasswordGenerator.generateOTP(); // generating the otp

        generateOTP = this.verifyTokenUniqueness(generateOTP);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expiryDuration);

        var otp = ForgotPassword.builder()
                .token(generateOTP)
                .expiresAt(expiresAt)
                .user(user)
                .build();

        this.forgotPasswordRepository.save(otp); //save the entry
        return new OtpInternalCarrier(generateOTP, expiryDuration);
    }


    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {

        var newPassword = resetPasswordRequest.newPassword();
        var newConfirmedPassword = resetPasswordRequest.newConfirmedPassword();

        if(!newConfirmedPassword.equals(newPassword))
            throw new IllegalArgumentException("New Password and Confirm Password do not match");


        var user = this.userRepository.findByEmailOrThrow(resetPasswordRequest.email());
        var forgotPassword = this.forgotPasswordRepository.findByUserOrThrow(user.getId());

        if(forgotPassword.isExpired())
            throw new ForgotPasswordExistsException("OTP has expired.Please Request for a new one time password");

        if(!forgotPassword.getToken().equals(resetPasswordRequest.otp()))
            throw new ForgotPasswordExistsException("Invalid OTP");

        var hashedPassword = this.passwordEncoder.encode(newPassword); //hash the new password
        user.setPassword(hashedPassword); //set the new password

        this.userRepository.save(user); //save the new password
        this.forgotPasswordRepository.delete(forgotPassword);
    }


    @Override
    @Transactional
    protected void removedExpiredCredentials() {

        log.info("Removing expired forgot passwords");
        var allForgotPasswords = this.forgotPasswordRepository.findAll();
        var expiredForgotPasswords = allForgotPasswords.stream()
                .filter(ForgotPassword::isExpired)
                .toList();

        log.info("Deleting {} expired forgot passwords", expiredForgotPasswords.size());
        this.forgotPasswordRepository.deleteAll(expiredForgotPasswords);
        log.info("Deleted {} expired forgot passwords", expiredForgotPasswords.size());
    }

    @Override
    protected String verifyTokenUniqueness(String token) {

        while (forgotPasswordRepository.existsByToken(token))
            token = oneTimePasswordGenerator.generateOTP();
        return token;
    }

}
