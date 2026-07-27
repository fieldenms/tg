package ua.com.fielden.platform.test;

import org.junit.runner.RunWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.ZoneId;

/// When this annotation is present on a JUnit test method, the test is ignored unless the time zone specified in the annotation
/// matches [the default time zone][ZoneId#systemDefault()].
///
/// Matching is by zone rules, not by ID, so equivalent zones are considered a match.
/// For example, `UTC` matches a default of `Etc/UTC`, and `America/New_York` matches `US/Eastern`.
/// This matters because CI and containerised JVMs commonly resolve the default zone to such an alias.
///
/// Note that the test runner, specified in [RunWith], must support this annotation for it to have effect.
///
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequireTimeZone {

    /// A time zone ID, as accepted by [ZoneId#of(String)].
    /// A common accepted format is `{area}/{city}` (e.g., `Australia/Melbourne`).
    ///
    /// An unrecognised value causes the test to fail rather than be ignored,
    /// so that a typo surfaces immediately instead of silently skipping the test in every environment.
    ///
    String value();

}
