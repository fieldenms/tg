package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ua.com.fielden.platform.security.user.User;

/**
 * Automatically assigns the value of the current user to annotated properties with <code>null</code> values, which happens at the time of saving.
 * Should be used to annotate properties of type {@link User} or {@link String} (in the latter case user's name is used).
 *
 * @author TG Team
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface TransactionUser {
}
