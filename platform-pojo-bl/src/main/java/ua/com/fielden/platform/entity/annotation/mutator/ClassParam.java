package ua.com.fielden.platform.entity.annotation.mutator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.Inject;

/**
 * Represents a BCE handler parameter of non-ordinary type. The provided class in attribute {@code value} can be used in 2 context - as a class (i.e., as provided) or to create an instance of that class.
 * In cases where it is used to create an instance, instantiation happens upon a handler creation using an injector.
 * Therefore, an instance can only be created successfully if the provided class is either:
 * <ul>
 * <li>An interface bound in the application IoC module, or
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
