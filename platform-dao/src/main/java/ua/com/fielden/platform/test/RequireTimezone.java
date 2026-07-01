package ua.com.fielden.platform.test;

import org.junit.runner.RunWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.ZoneId;
import java.util.TimeZone;

/// When this annotation is present on a JUnit test method, it will be ignored if the timezone specified in the annotation
/// does not match [the default timezone][TimeZone#getDefault()].
///
/// Note that the test runner, specified in [RunWith], must support this annotation for it to have effect.
///
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequireTimezone {

    /// A timezone ID, as accepted by [ZoneId#of(String)].
    /// A common accepted format is `{area}/{city}` (e.g., `Australia/Melbourne`).
    ///
    String value();

}
