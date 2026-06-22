package org.tracker.ubus.ubus.AnnotationProcessing.AnnotationImpl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.AnnotationProcessing.Annotations.AdminInvitationCodeRequired;
import org.tracker.ubus.ubus.Components.Auth.DTOs.Requests.RegisterRequest;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;


/**
 * Validates the invitation code requirement for users with the role "Admin".
 *
 * This class implements the {@code ConstraintValidator} interface, enabling it to
 * validate whether a provided {@code RegisterRequest} satisfies a specific constraint.
 * The validation ensures that if the user's role is "Admin", the invitation code in the
 * registration request is non-null and non-blank. For users with roles other than "Admin",
 * the presence of an invitation code is not required.
 *
 * The constraint being validated is indicated by the {@code AdminInvitationCodeRequired} annotation.
 * This annotation specifies the validation error message, supported validation groups,
 * and payload information.
 */
@Component
public class AdminInvitationCodeValidator implements ConstraintValidator<AdminInvitationCodeRequired,
        RegisterRequest> {

    @Override
    public boolean isValid(RegisterRequest value, ConstraintValidatorContext context) {

        var userRole = UserRole.ADMIN.getLabel();
        var role = value.role();
        if(role.equalsIgnoreCase(userRole))
            return value.invitationCode() != null && !value.invitationCode().isBlank();
        return true; // for other roles its fine so we can move on
    }
}
