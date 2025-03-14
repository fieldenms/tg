package ua.com.fielden.platform.test;

import ua.com.fielden.platform.entity.query.DbVersion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a test method to indicate that it should be run only if one of the specified databases is currently in use.
 * Otherwise, the annotated test method is ignored.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithDbVersion {

    DbVersion[] value();

}
