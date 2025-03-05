package ua.com.fielden.platform.entity.annotation;

import ua.com.fielden.platform.entity.meta.PropertyDescriptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Disables introspection of the annotated element.
 * The meaning depends on the kind of annotated element:
 * <ul>
 *   <li> Entity type - excluded for domain introspection, such as GraphQL introspection queries and Domain Explorer.
 *   <li> Property - cannot be modelled with {@link PropertyDescriptor}.
 *        Such properties are therefore excluded from autocompleters for property descriptors.
 * </ul>
 *
 * @author TG Team
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface DenyIntrospection {
}
