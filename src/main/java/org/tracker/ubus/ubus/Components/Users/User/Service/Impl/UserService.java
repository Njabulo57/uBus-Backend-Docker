package org.tracker.ubus.ubus.Components.Users.User.Service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Auth.Events.OtpEmailVerificationEvent;
import org.tracker.ubus.ubus.Components.Auth.Exception.External.InvalidCredentialsException;
import org.tracker.ubus.ubus.Components.Auth.Service.Impl.AuthService;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Repository.BusPreferenceRepository;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.Publisher.MultiEvenPublisher;
import org.tracker.ubus.ubus.Components.OneTimePassword.DTOs.Internal.OtpInternalCarrier;
import org.tracker.ubus.ubus.Components.OneTimePassword.Service.Impl.OneTimePasswordService;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Repository.TripRepository;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Repository.TripUserRepository;
import org.tracker.ubus.ubus.Components.Users.User.DTOs.Requests.EditUserDTO;
import org.tracker.ubus.ubus.Components.Users.User.DTOs.Responses.UserProfileResponse;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus;
import org.tracker.ubus.ubus.Components.Users.User.Mapper.UserMapper;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;
import org.tracker.ubus.ubus.Components.Users.User.Service.Interface.IUserService;
import org.tracker.ubus.ubus.Configuration.Security.UserPrincipal;

import java.util.UUID;

import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus.*;


@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserMapper userMapper;
    private final AuthService authService;
    public final MultiEvenPublisher publisher;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OneTimePasswordService oneTimePasswordService;
    private final BusPreferenceRepository busPreferenceRepository;
    private final TripUserRepository tripUserRepository;
    private final TripRepository tripRepository;


    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getUser();
    }


    @Override
    public UserProfileResponse viewProfile() {


        var user = getCurrentUser();

        var busPreference = busPreferenceRepository.findAllByUser(user);

        var totalTrips = 0;
        if(user.getRole() == UserRole.STAFF || user.getRole() == UserRole.STUDENT)
            totalTrips = this.tripUserRepository.countByUser(user);

        return this.userMapper.toDTO(user, busPreference, totalTrips);
    }

    @Transactional
    @Override
    public UserProfileResponse editProfile(EditUserDTO editUserDTO) {

        User currentUser = getCurrentUser();

        if (editUserDTO.getFirstname() != null) {
            currentUser.setFirstname(editUserDTO.getFirstname());
        }
        if (editUserDTO.getLastname() != null) {
            currentUser.setLastname(editUserDTO.getLastname());
        }
        if (editUserDTO.getEmail() != null) {

        }
        if(editUserDTO.getNewPassword() != null && editUserDTO.getOldPassword() != null)
            {
              if(passwordEncoder.matches(editUserDTO.getOldPassword(), currentUser.getPassword()))
                  {
                      final var encodedPassword = this.passwordEncoder.encode(editUserDTO.getNewPassword());
                      currentUser.setPassword(encodedPassword);
                  }
              else
                  throw new InvalidCredentialsException("Old password is incorrect");
            }
        if(editUserDTO.getPhoneNumber() != null)
            {
                currentUser.setPhoneNumber(editUserDTO.getPhoneNumber());
            }


        userRepository.save(currentUser);
        return this.userMapper.toDTO(currentUser, busPreferenceRepository.findAllByUser(currentUser), 0);
    }


    @Override
    @Transactional
    public boolean forgotPassword(String email) {
        if(userRepository.existsByEmail(email)) {
            User user = userRepository.findByEmail(email).orElse(null);
            if(user != null) {
                if(user.getStatus() == ACTIVE)
                {
                   OtpInternalCarrier otpInternalCarrier = oneTimePasswordService.generateAuthToken(user.getId());
                   publisher.publish(new OtpEmailVerificationEvent(this, user, otpInternalCarrier));
                }
                else
                    throw new InvalidCredentialsException("Email doesnt exist or isn't active");
            }
            else
                throw new InvalidCredentialsException("Email doesnt exist or isn't active");
        }
        else  {
            throw new InvalidCredentialsException("Email doesnt exist or isn't active");
        }

        return true;
    }


    @Transactional
    public boolean changePassword(String email, String newPassword, String otp) {
       if(oneTimePasswordService.validateOTP(otp))
       {
           User user = userRepository.findByEmail(email).orElse(null);
           if(user != null && user.getStatus() == ACTIVE) {
               user.setPassword(passwordEncoder.encode(newPassword));
           }
           else
               throw new InvalidCredentialsException("Email doesnt exist or isn't active");
       }
       else
           throw new InvalidCredentialsException("OTP doesnt exist");
       return true;
    }


    @Override
    public void deactivateAccount(String password) {
        User currentUser = getCurrentUser();
        if(currentUser != null) {
            if(passwordEncoder.matches(password, currentUser.getPassword())) {
                authService.logout();
                currentUser.setStatus(UserStatus.INACTIVE);
                userRepository.save(currentUser);
            } else {
                throw new InvalidCredentialsException("Invalid password");
            }
        }
    }

    @Override
    public void assignNfcCode(String nfcCode) {
        User currentUser = getCurrentUser();
        if(currentUser != null) {
            if(currentUser.getNfcCode() != null && !currentUser.getNfcCode().isBlank()) {
                throw new InvalidCredentialsException("NFC code already assigned");
            }
            currentUser.setNfcCode(nfcCode);
            userRepository.save(currentUser);
        } else {
            throw new InvalidCredentialsException("User not found");
        }
    }

    @Override
    public void adminAssignNfc(String nfcCode, UUID userId) {
        User user = userRepository.findByIdOrThrow(userId);
        user.setNfcCode(nfcCode);
        userRepository.save(user);
    }

    @Override
    public String getNfcCode() {
        var user = getCurrentUser();
        return user.getNfcCode();
    }

    @Override
    public boolean hasTrip() {
        var user = getCurrentUser();
        int activeTripCount = tripRepository.countActiveTripsByDriver(user);
        return activeTripCount > 0;
    }
}
