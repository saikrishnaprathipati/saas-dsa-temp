package uk.gov.saas.dsa.domain.validation;
   
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
  

@Target( {FIELD, ElementType.METHOD })
@Retention(RUNTIME)
@jakarta.validation.Constraint(validatedBy = MaxStringValidator.class)
public @interface MaxString {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends jakarta.validation.Payload>[] payload() default {};

    int value();
}
