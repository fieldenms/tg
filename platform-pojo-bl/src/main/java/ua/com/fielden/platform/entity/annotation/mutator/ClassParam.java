package ua.com.fielden.platform.entity.annotation.mutator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.Inject;

/**
 * Represents a BCE handler parameter of non-ordinary type. The provided class is instantiated upon handler creation using an instance of injector. Thus, the specified class value
 * can be only one of the following:
 * <ul>
 * <li>An interface bound in the application IoC module.
 * <li>A class with default constructor or a constructor annotated with {@link Inject} annotation.
 * </ul>
 * 
 * @author TG Team
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE })
public @interface ClassParam {
    String name();

    Class<?> value();
}
