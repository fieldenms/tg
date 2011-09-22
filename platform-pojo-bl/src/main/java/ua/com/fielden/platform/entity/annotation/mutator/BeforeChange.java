package ua.com.fielden.platform.entity.annotation.mutator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to be used for annotating property's mutators in order to process the value being set.
 * <p>
 * The primary usage of this annotation is to provide property validators to ensure that only correct values can be set.
 * <p>
 * A single parameter <code>value</code> accepts a list of handler specifications (refer {@link Handler} for more details).
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface BeforeChange {
    /**
     * An list of handlers to be executed upon property mutator invocation or re-validation of the property.
     *
     * @return
     */
    Handler[] value();
}
