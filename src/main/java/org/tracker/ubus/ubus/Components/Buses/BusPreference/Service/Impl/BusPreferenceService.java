package org.tracker.ubus.ubus.Components.Buses.BusPreference.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPreferenceResponse;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Exceptions.BusPreferenceAlreadyExistsException;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Exceptions.BusPreferenceMaximumException;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Exceptions.BusPreferenceNotFoundException;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Mapper.BusPreferenceMapper;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Repository.BusPreferenceRepository;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Service.Interface.IBusPreferenceService;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;
import org.tracker.ubus.ubus.Configuration.Security.UserPrincipal;

import java.util.Objects;

@Service
public class BusPreferenceService implements IBusPreferenceService {
    @Autowired
    BusPreferenceRepository repository;
    @Override
    public BusPreferenceResponse addPreference(Route route) {

        User user = getCurrentUser();
    boolean alreadyExists = repository.existsByUserAndRoute(user, route);
    if (alreadyExists) {
        throw  new BusPreferenceAlreadyExistsException("BusPreference is already set for user");
    }

    int count = repository.countByUser(user);
    if(count >=3)
    {
        throw new BusPreferenceMaximumException("User already has maximum number of bus preferences set");
    }
        BusPreference busPreference = new BusPreference(null,user,route);
        return new BusPreferenceMapper().toResponse(java.util.Collections.singletonList(repository.save(busPreference)));
    }

    @Override
    public BusPreferenceResponse editPreference(Route oldRoute, Route newRoute) {
       deletePreference(oldRoute);
       return addPreference(newRoute);
    }

    @Override
    public void deletePreference(Route oldRoute) {
        if(repository.existsByUserAndRoute(getCurrentUser(),oldRoute))
        {
            repository.delete(repository.findByUserAndRoute(getCurrentUser(), oldRoute));
        }
        else{
            throw new BusPreferenceNotFoundException("BusPreference not found");
        }
    }

    @Override
    public BusPreferenceResponse viewPreferences() {
        return new BusPreferenceMapper().toResponse(repository.findAllByUser(getCurrentUser()));
    }


    private User getCurrentUser() {
        UserPrincipal principal = (UserPrincipal) Objects.requireNonNull(SecurityContextHolder.getContext()
                        .getAuthentication())
                .getPrincipal();
        return Objects.requireNonNull(principal).getUser();
    }
}
