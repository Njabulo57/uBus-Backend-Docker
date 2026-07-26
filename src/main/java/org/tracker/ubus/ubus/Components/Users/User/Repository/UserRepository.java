package org.tracker.ubus.ubus.Components.Users.User.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Auth.Exception.Internal.UserNotFoundException;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {




    Optional<User> findByNfcCode(String nfcCode);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndStatus(String email, UserStatus status);


    @Query("SELECT u FROM User u WHERE u.status = :status AND u.role = :role")
    Page<User> findByStatusAndRole(UserStatus status, UserRole role, Pageable pageable);

    List<User> findByStatusAndRole(UserStatus status, UserRole role);
    List<User> findByStatus(UserStatus status);
    List<User> findByRole(UserRole role);

    List<User> findByIdIn(Collection<UUID> ids);

    List<User> findByRoleIn(List<UserRole> roles);

    List<User> findByRoleInAndStatus(List<UserRole> role, UserStatus status);

    List<User> findByRoleAndStatus(UserRole role, UserStatus status);

    boolean existsByEmail(String email);


    default User findByEmailOrThrow(String email) throws UserNotFoundException {
        return this.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    default User findByIdOrThrow(UUID id) throws UserNotFoundException {
        return this.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    default User findByNfcCodeOrThrow(String nfcCode) {
        return this.findByNfcCode(nfcCode)
                .orElseThrow(() -> new UserNotFoundException("User not found with nfcCode: " + nfcCode));

    }

}
