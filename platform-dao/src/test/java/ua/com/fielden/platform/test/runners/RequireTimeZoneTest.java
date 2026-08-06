package ua.com.fielden.platform.test.runners;

import net.bytebuddy.ByteBuddy;
import org.junit.Test;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import ua.com.fielden.platform.test.RequireTimeZone;
import ua.com.fielden.platform.test.exceptions.DomainDrivenTestException;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.test_config.H2OrPostgreSqlOrSqlServerContextSelector;

import java.lang.reflect.Modifier;
import java.time.DateTimeException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.test_utils.TestUtils.withTimeZone;

/// Verifies that the TG test runner correctly decides whether a test method should be ignored based on the [RequireTimeZone] annotation.
///
/// The test constructs the runner directly against fixture classes and invokes the ignore predicate without running the actual tests.
///
public class RequireTimeZoneTest extends AbstractDaoTestCase {

    private static final String TZ_PARIS = "Europe/Paris";
    private static final String TZ_UTC = "UTC";

    @Test
    public void method_is_ignored_only_when_the_required_time_zone_does_not_match_the_default_one() throws Exception {
        withTimeZone(TZ_PARIS, () -> {
            final var runner = new H2OrPostgreSqlOrSqlServerContextSelector(MyTest.class);
            assertTrue("Method should be ignored when @RequireTimeZone does not match the default time zone.",
                       runner.isIgnored(findMethod(runner, "test_in_utc")));
            assertFalse("Method should not be ignored when @RequireTimeZone matches the default time zone.",
                        runner.isIgnored(findMethod(runner, "test_in_paris")));
            assertFalse("Method should not be ignored when @RequireTimeZone is absent.",
                        runner.isIgnored(findMethod(runner, "test_anywhere")));
        });

        withTimeZone(TZ_UTC, () -> {
            final var runner = new H2OrPostgreSqlOrSqlServerContextSelector(MyTest.class);
            assertTrue("Method should be ignored when @RequireTimeZone does not match the default time zone.",
                       runner.isIgnored(findMethod(runner, "test_in_paris")));
            assertFalse("Method should not be ignored when @RequireTimeZone matches the default time zone.",
                        runner.isIgnored(findMethod(runner, "test_in_utc")));
            assertFalse("Method should not be ignored when @RequireTimeZone is absent.",
                        runner.isIgnored(findMethod(runner, "test_anywhere")));
        });
    }

    @Test
    public void an_invalid_required_time_zone_fails_only_its_own_method_and_does_not_abort_the_class() throws Exception {
        final var testClass = new ByteBuddy().subclass(AbstractTestWithInvalidTimeZone.class)
                .name(String.join(".", this.getClass().getPackageName(), "TestWithInvalidTimeZone"))
                .modifiers(Modifier.PUBLIC)
                .make()
                .load(getClass().getClassLoader())
                .getLoaded();

        final var runner = new H2OrPostgreSqlOrSqlServerContextSelector(testClass);
        final var invalidMethod = findMethod(runner, "test_with_invalid_time_zone");

        assertThatThrownBy(() -> runner.isIgnored(invalidMethod))
                .as("An invalid @RequireTimeZone should fail loudly rather than silently ignore the test.")
                .isInstanceOf(DomainDrivenTestException.class)
                .as("The underlying time zone parsing error should be preserved as the cause.")
                .cause().isInstanceOf(DateTimeException.class);

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
                     "test_with_invalid_time_zone", failure.getDescription().getMethodName());
        assertTrue("The failure should carry a DomainDrivenTestException.",
                   failure.getException() instanceof DomainDrivenTestException);
    }

    @Override
    protected void populateDomain() {}

    // ---- Fixture classes used as input to the runner under test ----

    public static class MyTest extends AbstractDaoTestCase {
        @Test
        @RequireTimeZone(TZ_PARIS)
        public void test_in_paris() {}

        @Test
        @RequireTimeZone(TZ_UTC)
        public void test_in_utc() {}

        @Test
        public void test_anywhere() {}

        @Override
        protected void populateDomain() {}
    }

    /// This test class has to be abstract to align with the default test runner configuration in IntelliJ IDEA.
    /// A concrete class extending this one should be generated at runtime rather than declared in source code.
    /// When declared in source code, IDEA picks up the deliberately invalid method, runs it, and the test naturally fails.
    /// Maven's Surefire does not do this -- its default filter excludes inner classes.
    ///
    public static abstract class AbstractTestWithInvalidTimeZone extends AbstractDaoTestCase {
        @Test
        @RequireTimeZone("I don‘t exist")
        public void test_with_invalid_time_zone() {}

        @Override
        protected void populateDomain() {}
    }

    // ---- Helpers ----

    private static FrameworkMethod findMethod(final H2OrPostgreSqlOrSqlServerContextSelector runner, final String name) {
        return runner.getTestClass().getAnnotatedMethods(Test.class).stream()
                .filter(m -> m.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Fixture is missing test method [%s].".formatted(name)));
    }

}
