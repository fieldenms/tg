package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation should be used to indicate properties of an entity type that should not be validated for entity existence.
 * Semantically this suggests that such values should be most likely persisted at the time of saving their owning entity.
 * However, at this stage such semantics should be implemented at the domain level rather than generically at the platform level.
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Autosave {
}
