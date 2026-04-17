package com.example.valtrak.GameData.Config.Annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NationValidator.class)
public @interface ValidNation {
    String message() default "Invalid nation";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
