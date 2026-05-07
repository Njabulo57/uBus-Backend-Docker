package org.tracker.ubus.ubus.Components.User.Service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.BusPreference.Repository.BusPreferenceRepository;
import org.tracker.ubus.ubus.Components.User.DTOs.Responses.UserProfileResponse;
import org.tracker.ubus.ubus.Components.User.Entity.User;
import org.tracker.ubus.ubus.Components.User.Mapper.UserMapper;
import org.tracker.ubus.ubus.Components.User.Service.Interface.IUserService;
import org.tracker.ubus.ubus.Configuration.Security.UserPrincipal;


@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserMapper userMapper;
    private final BusPreferenceRepository busPreferenceRepository;
    @Override
    public UserProfileResponse viewProfile() {

        var authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        var userPrincipal = (UserPrincipal) authentication.getPrincipal();
        var user = userPrincipal.getUser();

        var busPreference = busPreferenceRepository.findByUser(user.getId())
                .orElse(null);

        return this.userMapper.toDTO(user, busPreference);
    }
}
