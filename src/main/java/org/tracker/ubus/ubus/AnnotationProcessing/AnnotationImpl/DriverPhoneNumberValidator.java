package org.tracker.ubus.ubus.AnnotationProcessing.AnnotationImpl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.tracker.ubus.ubus.AnnotationProcessing.Annotations.DriverPhoneRequired;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.RegisterRequest;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;

public class DriverPhoneNumberValidator implements ConstraintValidator<DriverPhoneRequired,
        RegisterRequest> {


    @Override
    public boolean isValid(RegisterRequest value, ConstraintValidatorContext context) {

        var role = UserRole.DRIVER.getLabel();
        if(role.equalsIgnoreCase(value.role())) //if the user role is driver it is mandatory to get their phone number
            return value.phoneNumber() != null && !value.phoneNumber().isBlank();
        //for other roles its fine so we can move on
        return true;
    }
}
