package org.tracker.ubus.ubus.AnnotationProcessing.Annotations;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
public @interface DriverPhoneRequired {


    String message() default "Phone number is required";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

