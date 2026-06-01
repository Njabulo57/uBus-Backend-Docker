package org.tracker.ubus.ubus.Components.Users.User.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Auth.Exception.Internal.UserNotFoundException;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {


    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndStatus(String email, UserStatus status);


    @Query("SELECT u FROM User u WHERE u.status = :status AND u.role = :role")
    List<User> findByStatusAndRole(UserStatus status, UserRole role);
    List<User> findByStatus(UserStatus status);
    List<User> findByRole(UserRole role);





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
