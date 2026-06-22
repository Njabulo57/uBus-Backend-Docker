package org.tracker.ubus.ubus.AnnotationProcessing.Annotations;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.tracker.ubus.ubus.AnnotationProcessing.AnnotationImpl.AdminInvitationCodeValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {AdminInvitationCodeValidator.class})
public @interface AdminInvitationCodeRequired {

    String message() default "Invitation code is required";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
