package org.tracker.ubus.ubus.Components.Users.User.Service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.EmailOtpRequest;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.RegisterRequest;
import org.tracker.ubus.ubus.Components.Auth.Exception.External.DuplicateEmailException;
import org.tracker.ubus.ubus.Components.Auth.Exception.External.InvalidCredentialsException;
import org.tracker.ubus.ubus.Components.Auth.Service.Impl.AuthService;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Repository.BusPreferenceRepository;
import org.tracker.ubus.ubus.Components.Users.User.DTOs.Requests.EditUserDTO;
import org.tracker.ubus.ubus.Components.Users.User.DTOs.Responses.UserProfileResponse;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus;
import org.tracker.ubus.ubus.Components.Users.User.Mapper.UserMapper;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;
import org.tracker.ubus.ubus.Components.Users.User.Service.Interface.IUserService;
import org.tracker.ubus.ubus.Configuration.Security.UserPrincipal;

import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole.STUDENT;
import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus.ADMIN_APPROVAL_PENDING;
import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus.EMAIL_APPROVAL_PENDING;


@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserMapper userMapper;
    private final BusPreferenceRepository busPreferenceRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getUser();
    }
    @Override
    public UserProfileResponse viewProfile() {


        var user = getCurrentUser();

        var busPreference = busPreferenceRepository.findAllByUser(user);

        return this.userMapper.toDTO(user, busPreference);
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
              if(editUserDTO.getNewPassword().equals(editUserDTO.getOldPassword()))
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
        return this.userMapper.toDTO(currentUser, busPreferenceRepository.findAllByUser(currentUser));
    }
}
