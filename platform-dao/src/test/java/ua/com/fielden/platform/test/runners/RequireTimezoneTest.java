package ua.com.fielden.platform.test.runners;

import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import ua.com.fielden.platform.test.RequireTimezone;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.test_config.H2OrPostgreSqlOrSqlServerContextSelector;

import java.util.TimeZone;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/// Verifies that the TG test runner correctly decides whether a test method should be ignored based on the [RequireTimezone] annotation.
///
/// The test constructs the runner directly against fixture classes and invokes the ignore predicate without running the actual tests.
///
public class RequireTimezoneTest extends AbstractDaoTestCase {

    private static final String TZ_PARIS = "Europe/Paris";
    private static final String TZ_UTC = "UTC";

    @Test
    public void method_is_ignored_only_when_the_required_timezone_does_not_match_the_default_one() throws Exception {
        withTimezone(TZ_PARIS, () -> {
            final var runner = new H2OrPostgreSqlOrSqlServerContextSelector(MyTest.class);
            assertTrue("Method should be ignored when @RequireTimezone does not match the default timezone.",
                       runner.isIgnored(findMethod(runner, "test_in_utc")));
            assertFalse("Method should not be ignored when @RequireTimezone matches the default timezone.",
                        runner.isIgnored(findMethod(runner, "test_in_paris")));
            assertFalse("Method should not be ignored when @RequireTimezone is absent.",
                        runner.isIgnored(findMethod(runner, "test_anywhere")));
        });

        withTimezone(TZ_UTC, () -> {
            final var runner = new H2OrPostgreSqlOrSqlServerContextSelector(MyTest.class);
            assertTrue("Method should be ignored when @RequireTimezone does not match the default timezone.",
                       runner.isIgnored(findMethod(runner, "test_in_paris")));
            assertFalse("Method should not be ignored when @RequireTimezone matches the default timezone.",
                        runner.isIgnored(findMethod(runner, "test_in_utc")));
            assertFalse("Method should not be ignored when @RequireTimezone is absent.",
                        runner.isIgnored(findMethod(runner, "test_anywhere")));
        });
    }

    @Test
    public void method_is_ignored_if_the_required_timezone_is_invalid() throws Exception {
        final var runner = new H2OrPostgreSqlOrSqlServerContextSelector(MyTest.class);
        assertTrue("Method should be ignored when @RequireTimezone does not match the default timezone.",
                   runner.isIgnored(findMethod(runner, "test_with_invalid_timezone")));
    }

    // ---- Fixture classes used as input to the runner under test ----

    public static class MyTest extends AbstractDaoTestCase {
        @Test
        @RequireTimezone(TZ_PARIS)
        public void test_in_paris() {}

        @Test
        @RequireTimezone(TZ_UTC)
        public void test_in_utc() {}

        @Test
        public void test_anywhere() {}

        @Test
        @RequireTimezone("I don't exist")
        public void test_with_invalid_timezone() {}
    }

    // ---- Helpers ----

    private static FrameworkMethod findMethod(final H2OrPostgreSqlOrSqlServerContextSelector runner, final String name) {
        return runner.getTestClass().getAnnotatedMethods(Test.class).stream()
                .filter(m -> m.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Fixture is missing test method [%s].".formatted(name)));
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    /// Sets the JVM default timezone for the duration of `action` and restores it afterwards.
    /// Single-threaded test execution is assumed, in line with the project-wide convention.
    ///
    private static void withTimezone(final String tzId, final ThrowingRunnable action) throws Exception {
        final TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone(tzId));
            action.run();
        } finally {
            TimeZone.setDefault(original);
        }
    }

}
