package ua.com.fielden.platform.entity.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ua.com.fielden.platform.companion.IEntityReader;

/**
 * Should be used to annotate non-persistent entity types that support {@link EntityExists} validation.
 * That is, properties of those types should be recognized as such that required {@link EntityExists} validation.
 * <p>
 * Under the normal circumstances only persistent entity types should have this validation.
 * However, there are cases where non-persistent entities with ad-hoc values (e.g. returned from value matchers) should be treated as if they are persistent.
 * Corresponding companion objects must override method {@link IEntityReader#entityExists(ua.com.fielden.platform.entity.AbstractEntity)} to supply the right logic to look for such non-persisted values.
 * <p>
 * A good example of the use for this support are various analyses and reports having {@code groupBy} crit-only properties.
 * Values for such criteria are usually pre-defined, but not persisted.
 * 
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface SupportsEntityExistsValidation {
}
