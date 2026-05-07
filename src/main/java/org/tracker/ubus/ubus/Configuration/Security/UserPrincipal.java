package org.tracker.ubus.ubus.Configuration.Security;


import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.tracker.ubus.ubus.Components.User.Entity.User;
import org.tracker.ubus.ubus.Components.User.Enum.UserStatus;

import java.util.ArrayList;
import java.util.Collection;



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


    public String getRole() {
        return user.getRole().getLabel();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

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

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

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
