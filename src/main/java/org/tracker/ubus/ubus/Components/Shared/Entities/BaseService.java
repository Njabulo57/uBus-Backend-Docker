package org.tracker.ubus.ubus.Components.Shared.Entities;


import org.springframework.security.core.context.SecurityContextHolder;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Configuration.Security.UserPrincipal;

/**
 * An abstract base service class providing shared functionality for accessing and managing
 * user authentication details within the security context of the application. This class
 * serves as a utility for operations related to the currently authenticated user and their
 * principal.
 */
public abstract class BaseService {


    /**
     * Retrieves the currently logged-in user from the SecurityContext.
     * The currently authenticated user's details are obtained from the
     * {@link SecurityContextHolder}. The method fetches the {@link User}
     * entity by invoking {@link UserPrincipal#getUser()}.
     *
     * @return the {@link User} entity representing the currently authenticated user
     * @throws IllegalStateException if the user object has already been accessed once
     */
    protected final User getCurrentUser() {

        var principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return principal.getUser();
    }


    /**
     * Retrieves the authenticated user's principal from the security context.
     * The principal contains user-specific details such as roles, permissions,
     * and access-related information.
     *
     * @return the {@link UserPrincipal} object representing the authenticated user
     */
    protected final UserPrincipal getUserPrincipal() {
        return (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

}
