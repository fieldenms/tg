package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that should be used for declaring the type of entity key.
 * <p>
 * Java does not store type parameters at runtime, however quite often it is a requirement to know the exact type of an entity key when only entity class is known available (only
 * an instance). This annotation is a compromise between the need to know key type at runtime and having a developer to do an extra little to specify this annotation on every
 * concrete entity class.
 *
 * @author 01es
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface KeyType {
    @SuppressWarnings("rawtypes")
    Class<? extends Comparable> value();
}
