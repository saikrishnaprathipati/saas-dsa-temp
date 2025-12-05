package uk.gov.saas.dsa.domain.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;


@Target( {FIELD, ElementType.METHOD })
@Retention(RUNTIME)
@Constraint(validatedBy = MinStringValidator.class)

public @interface MinString {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int value();
}
