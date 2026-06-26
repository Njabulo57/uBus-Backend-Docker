package org.tracker.ubus.ubus.Components.Buses.BusPreference.Service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Request.BusPreferenceDTO;
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

@Transactional
@Service
public class BusPreferenceService implements IBusPreferenceService {
    @Autowired
    BusPreferenceRepository repository;
    @Override
    public BusPreferenceResponse addPreference(BusPreferenceDTO busPreferenceDTO) {

       if(Objects.isNull(busPreferenceDTO)) {
           throw new BusPreferenceNotFoundException("BusPreference not found");
       }
       if(busPreferenceDTO.getNewRoute() == null) {
           throw new BusPreferenceNotFoundException("New route not found");
       }
       if(busPreferenceDTO.getNewRoute().isBlank())
       {
           throw new BusPreferenceNotFoundException("New route is empty");
       }
        Route newRoute = Route.valueOf(busPreferenceDTO.getNewRoute());
        User user = getCurrentUser();
    boolean alreadyExists = repository.existsByUserAndRoute(user, newRoute);
    if (alreadyExists) {
        throw  new BusPreferenceAlreadyExistsException("BusPreference is already set for user");
    }

    int count = repository.countByUser(user);
    if(count >=3)
    {
        throw new BusPreferenceMaximumException("User already has maximum number of bus preferences set");
    }
        BusPreference busPreference = new BusPreference(null,user,newRoute);
        return new BusPreferenceMapper().toResponse(java.util.Collections.singletonList(repository.save(busPreference)));
    }

    @Override
    public BusPreferenceResponse editPreference(BusPreferenceDTO busPreferenceDTO) {
        if(Objects.isNull(busPreferenceDTO)) {
            throw new BusPreferenceNotFoundException("BusPreference not found");
        }
        if(busPreferenceDTO.getNewRoute() == null) {
            throw new BusPreferenceNotFoundException("New route not found");
        }
        if(busPreferenceDTO.getOldRoute() == null) {
            throw new BusPreferenceNotFoundException("Old route not found");
        }
        if(busPreferenceDTO.getOldRoute().equals(busPreferenceDTO.getNewRoute())) {
            throw new BusPreferenceNotFoundException("Route is already set");
        }
       deletePreference(busPreferenceDTO);
       return addPreference(busPreferenceDTO);
    }

    @Override
    public void deletePreference(BusPreferenceDTO busPreferenceDTO) {

        if(Objects.isNull(busPreferenceDTO)) {
            throw new BusPreferenceNotFoundException("BusPreference not found");
        }
        if(busPreferenceDTO.getOldRoute() == null) {
            throw new BusPreferenceNotFoundException("Old route not found");
        }
        if(busPreferenceDTO.getOldRoute().isBlank())
        {
            throw new BusPreferenceNotFoundException("Old route is empty");
        }
        Route oldRoute = Route.valueOf(busPreferenceDTO.getOldRoute());

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
