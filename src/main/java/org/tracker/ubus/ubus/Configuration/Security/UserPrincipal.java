package org.tracker.ubus.ubus.Configuration.Security;


import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Represents the principal user details used for authentication and authorization purposes.
 * This class implements {@link UserDetails} to integrate with Spring Security.
 * It wraps a {@code User} entity and provides the necessary methods to retrieve user-related
 * information required for security operations.
 */
public class UserPrincipal implements UserDetails {

    // this reference is kept to avoid having to recreate the user from a db sql call again
    private final User user;
    private int issuedCount;
    private final Collection<GrantedAuthority> authorities =new ArrayList<>();

    public UserPrincipal(User user) {
        this.user = user;
        this.issuedCount = 0;
        addAuthorities();//adding the role of the user and their permissions
    }


    /**
     * for security reasons this user can only ever be accessed once
     * reason for this approach is to prevent always getting a reference of
     * this user as any un-intended changes to this object under a transaction can
     * alter its state in the database
     * @return returns the jpa user entity of the logged-in user
     * @throws IllegalStateException throws the exception if the user is used more than once
     */
    public User getUser() throws IllegalStateException{
        if(issuedCount == 0)
            return user;

        issuedCount = -1;
        throw new IllegalStateException("User Already Issued");
    }


    /**
     * Retrieves the label of the user's role.
     *
     * @return the label representing the user's role
     */
    public String getRole() {
        return user.getRole().getLabel();
    }


    /**
     * Retrieves the collection of authorities granted to the user.
     * This includes roles and permissions associated with the user,
     * which are used for access control in security operations.
     *
     * @return a collection of granted authorities for the user
     */
    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }


    /**
     * Retrieves the password associated with the user.
     * The password is securely stored and may be used for authentication purposes.
     *
     * @return the password of the user
     */
    @Override
    public @NonNull String getPassword() {
        return user.getPassword();
    }


    /**
     * Retrieves the username of the user.
     * The username is derived from the user's email address.
     *
     * @return the username, which corresponds to the user's email
     */
    @Override
    public @NonNull String getUsername() {
        return user.getEmail();
    }


    /**
     * Indicates whether the user's account has expired.
     * An expired account cannot be authenticated.
     *
     * @return {@code true} if the account is not expired, {@code false} otherwise
     */
    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }


    /**
     * if the user's email isn't approved in the system
     * then they are not allowed in the system
     * @return if the account is still locked or not
     */
    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() == UserStatus.ACTIVE;
    }


    /**
     * Indicates whether the user's credentials (e.g., password) have expired.
     * Expired credentials prevent the user from being authenticated.
     *
     * @return {@code true} if the credentials are not expired, {@code false} otherwise
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }


    /**
     * Indicates whether the user is currently enabled.
     * An enabled user is allowed to authenticate and access the system.
     *
     * @return {@code true} if the user is enabled, {@code false} otherwise
     */
    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }


    private void addAuthorities() {

        final String securityRole = getSecurityRole();
        this.authorities.add(new  SimpleGrantedAuthority(securityRole)); //adding a security role

    }


    private String getSecurityRole() {
        return user.getRole().getSecurityRole();
    }
}
