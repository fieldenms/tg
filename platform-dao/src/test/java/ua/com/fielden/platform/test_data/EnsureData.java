package ua.com.fielden.platform.test_data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Should be used to specify <code>populate*</code> methods that the current <code>populate*</code> depends on from the perspective of the data they populate.
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface EnsureData {

    /**
     * An array of method names. Order matters.
     *
     * @return
     */
    String[] value();

}