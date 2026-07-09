package ua.com.fielden.platform.test;

import ua.com.fielden.platform.entity.query.DbVersion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Annotates a test method or a test class to indicate that the test(s) should be run only if one of the specified databases is currently in use.
/// Otherwise, the annotated test method (or every test method in the annotated class) is ignored.
///
/// A method-level annotation takes precedence over a class-level one.
/// A class-level annotation is inherited by subclasses and applies to inherited test methods.
///
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface WithDbVersion {

    DbVersion[] value();

}
