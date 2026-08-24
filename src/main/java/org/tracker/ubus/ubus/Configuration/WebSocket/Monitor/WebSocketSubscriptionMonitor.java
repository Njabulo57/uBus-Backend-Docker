package org.tracker.ubus.ubus.Configuration.WebSocket.Monitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSubscriptionMonitor {

    private final SimpUserRegistry simpUserRegistry;
    private final Map<String, List<User>> connectedStaffAndStudents;

    public long countConnectionsTo(String destination) {
        var subscribers = simpUserRegistry.getUsers()
                .stream()
                .filter(simpUser -> isSubscribedTo(destination, simpUser))
                .count();
        log.info("There are {} subscribers to {}", subscribers, destination);
        return subscribers;
    }


    public boolean isEmptyOfConnectionsTo(String destination) {
        log.info("Checking if {} is empty", destination);

        return countConnectionsTo(destination) == 0;
    }

    public Set<User> getUsersSubscribedTo(String destination) {
//        var connectedSimpleUsers = simpUserRegistry.getUsers()
//                .stream()
//                .filter(simpUser -> isSubscribedTo(destination, simpUser)) //get the connected Users
//                .map(SimpUser::getName)
//                .toList();

        return getConnectedUsers(destination);
    }


    public Collection<String> getSubscriptionsForDestination(String destination) {

        return simpUserRegistry.getUsers()
                .stream()
                .filter(simpUser -> isSubscribedTo(destination, simpUser))
                .map(SimpUser::getName)
                .toList();
    }


    private boolean isSubscribedTo(String destination, SimpSession userSession) {
        return userSession.getSubscriptions()
               .stream()
               .anyMatch(connection -> connection.getDestination()
                       .contains(destination)
               );
    }

    private boolean isSubscribedTo(String destination, SimpUser user) {

        log.info("Checking if {} is subscribed to {}", user.getName(), destination);
        return user.getSessions().stream()
                .anyMatch(simpSession ->
                        isSubscribedTo(destination, simpSession));
    }


    private Set<User> getConnectedUsers(String destination) {
        return connectedStaffAndStudents.entrySet()
                .stream()
                .filter(entry -> entry.getKey().contains(destination))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableSet());
    }


}
