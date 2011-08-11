package ua.com.fielden.platform.reflection.asm.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface AnnotationForTesting {
    String value();
    int intValue();
    double doubleValue();
}
