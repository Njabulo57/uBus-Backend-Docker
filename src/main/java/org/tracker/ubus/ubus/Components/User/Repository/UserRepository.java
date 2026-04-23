package org.tracker.ubus.ubus.Components.User.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Auth.Exception.UserNotFoundException;
import org.tracker.ubus.ubus.Components.User.Entity.User;
import org.tracker.ubus.ubus.Components.User.Enum.UserStatus;


import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {


    Optional<User> findByEmail(String email);


    Optional<User> findByEmailAndStatus(String email, UserStatus status);


    default User findByEmailOrThrow(String email) throws UserNotFoundException {
        return this.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    default User findByIdOrThrow(UUID id) throws UserNotFoundException {
        return this.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    boolean existsByEmail(String email);
}
