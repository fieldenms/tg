package ua.com.fielden.platform.reflection.asm.impl.entities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Annotation1 {
    String value() default "default";

    double doubleValue();

    ENUM1 enumValue();

    public enum ENUM1 {
        E1, E2
    }
}
