package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that should be used for declaring entity properties used as part of the composite key.
 * <p>
 * Property <code>order</code> determines order in which properties are added into the composite key. There should be no properties with the same order, and there should be at
 * least one annotated property. Failure to comply with these requirements cause a runtime exception.
 * 
 * @author 01es
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface CompositeKeyMember {
    int value();
}
