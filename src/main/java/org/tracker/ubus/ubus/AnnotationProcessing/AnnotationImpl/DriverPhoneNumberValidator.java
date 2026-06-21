package org.tracker.ubus.ubus.AnnotationProcessing.AnnotationImpl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.tracker.ubus.ubus.AnnotationProcessing.Annotations.DriverPhoneRequired;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.RegisterRequest;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;

/**
 * Validates the phone number requirement for users with the role "Driver".
 * <p>
 * This class implements the {@code ConstraintValidator} interface, allowing it to
 * validate whether the provided {@code RegisterRequest} satisfies a specific constraint.
 * The validation ensures that if the user's role is "Driver", the phone number
 * in the registration request is non-null and non-blank. For users with roles other
 * than "Driver", the phone number may be null or blank.
 *
 * The constraint being validated is indicated by the {@code DriverPhoneRequired} annotation.
 * This annotation specifies the validation error message, as well as the supported validation groups
 * and payload information.
 *
 * Usage:
 * This validator is automatically invoked within the validation framework,
 * and no direct interaction is required by client code.
 */
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
