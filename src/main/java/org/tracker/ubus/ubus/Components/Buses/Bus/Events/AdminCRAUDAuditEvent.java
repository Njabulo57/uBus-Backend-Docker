package org.tracker.ubus.ubus.Components.Buses.Bus.Events;


import org.tracker.ubus.ubus.Components.Users.User.Entity.User;


public interface AdminCRAUDAuditEvent {


    default String formatAdminName(User user)
            throws NullPointerException {

        if(user == null) throw new NullPointerException("User cannot be null");

        var firstName = user.getFirstname();
        var lastName = user.getLastname();
        var firstNameCharCapitalized = Character.toUpperCase(firstName.charAt(0));
        return firstNameCharCapitalized + ". " + lastName;
    }

}
