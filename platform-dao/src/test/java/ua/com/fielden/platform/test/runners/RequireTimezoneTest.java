package ua.com.fielden.platform.test.runners;

import org.junit.Test;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import ua.com.fielden.platform.test.RequireTimezone;
import ua.com.fielden.platform.test.exceptions.DomainDrivenTestException;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.test_config.H2OrPostgreSqlOrSqlServerContextSelector;

import java.time.DateTimeException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.test_utils.TestUtils.withTimeZone;

/// Verifies that the TG test runner correctly decides whether a test method should be ignored based on the [RequireTimezone] annotation.
///
/// The test constructs the runner directly against fixture classes and invokes the ignore predicate without running the actual tests.
///
public class RequireTimezoneTest extends AbstractDaoTestCase {

    private static final String TZ_PARIS = "Europe/Paris";
    private static final String TZ_UTC = "UTC";

    @Test
    public void method_is_ignored_only_when_the_required_timezone_does_not_match_the_default_one() throws Exception {
        withTimeZone(TZ_PARIS, () -> {
            final var runner = new H2OrPostgreSqlOrSqlServerContextSelector(MyTest.class);
            assertTrue("Method should be ignored when @RequireTimezone does not match the default timezone.",
                       runner.isIgnored(findMethod(runner, "test_in_utc")));
            assertFalse("Method should not be ignored when @RequireTimezone matches the default timezone.",
                        runner.isIgnored(findMethod(runner, "test_in_paris")));
            assertFalse("Method should not be ignored when @RequireTimezone is absent.",
                        runner.isIgnored(findMethod(runner, "test_anywhere")));
        });

        withTimeZone(TZ_UTC, () -> {
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
    public void an_invalid_required_timezone_raises_an_exception_rather_than_silently_ignoring_the_test() throws Exception {
        final var runner = new H2OrPostgreSqlOrSqlServerContextSelector(MyTest.class);
        final var ex = assertThrows("An invalid @RequireTimezone should fail loudly rather than silently ignore the test.",
                                    DomainDrivenTestException.class,
                                    () -> runner.isIgnored(findMethod(runner, "test_with_invalid_timezone")));
        assertTrue("The underlying timezone parsing error should be preserved as the cause.",
                   ex.getCause() instanceof DateTimeException);
    }

    @Test
    public void an_invalid_required_timezone_fails_only_its_own_method_and_does_not_abort_the_class() throws Exception {
        final var runner = new H2OrPostgreSqlOrSqlServerContextSelector(MyTest.class);
        final var invalidMethod = findMethod(runner, "test_with_invalid_timezone");

        final var failures = new ArrayList<Failure>();
        final var notifier = new RunNotifier();
        notifier.addListener(new RunListener() {
            @Override
            public void testFailure(final Failure failure) {
                failures.add(failure);
            }
        });

        // Must return normally: were the exception to propagate out of runChild, JUnit would abort the whole class.
        runner.runChild(invalidMethod, notifier);

        assertEquals("Exactly one test failure should be reported.", 1, failures.size());
        final var failure = failures.get(0);
        assertEquals("The failure should be attributed to the offending method, not the class.",
                     "test_with_invalid_timezone", failure.getDescription().getMethodName());
        assertTrue("The failure should carry a DomainDrivenTestException.",
                   failure.getException() instanceof DomainDrivenTestException);
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
        @RequireTimezone("I don‘t exist")
        public void test_with_invalid_timezone() {}
    }

    // ---- Helpers ----

    private static FrameworkMethod findMethod(final H2OrPostgreSqlOrSqlServerContextSelector runner, final String name) {
        return runner.getTestClass().getAnnotatedMethods(Test.class).stream()
                .filter(m -> m.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Fixture is missing test method [%s].".formatted(name)));
    }

}
