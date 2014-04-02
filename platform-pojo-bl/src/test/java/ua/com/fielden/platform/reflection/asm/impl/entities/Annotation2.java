package ua.com.fielden.platform.reflection.asm.impl.entities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Annotation2 {
    String value() default "value";

    int intValue();

    Class<?> type() default Void.class;
}
