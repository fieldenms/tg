package ua.com.fielden.platform.test_utils;

import org.junit.Assert;
import org.junit.function.ThrowingRunnable;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.Assert.*;

/**
 * A collection of general-purpose test utilities.
 *
 * @author TG Team
 */
public final class TestUtils {

    private TestUtils() {}

    /**
     * Asserts that an optional is present and returns the value described by it.
     * To provide a custom message in case of assertion failure, see {@link #assertPresent(String, Optional)}.
     *
     * @param <T>   the type of the value described by the optional
     * @param opt   the {@link Optional} instance
     * @return  the value described by the Optional, if present
     */
    public static <T> T assertPresent(final Optional<T> opt) {
        return assertPresent("Optional is empty.", opt);
    }

    /**
     * Asserts that an optional is present and returns the value described by it.
     *
     * @param <T>       the type of the value described by the optional
     * @param message   the identifying message for the {@link AssertionError}
     * @param opt       the {@link Optional} instance
     * @return  the value described by the Optional, if present
     */
    public static <T> T assertPresent(final String message, final Optional<T> opt) {
        assertTrue(message, opt.isPresent());
        return opt.get();
    }

    /**
     * Like {@link #assertOptEquals(String, Object, Optional)} but uses a default message.
     */
    public static <T> T assertOptEquals(final T expected, final Optional<T> opt) {
        assertPresent("Optional is empty.", opt);
        assertEquals(expected, opt.get());
        return opt.get();
    }

    /**
     * If an optional is present and its underlying value is equal to the expected one, returns the underlying value.
     * Otherwise, fails with the given message.
     */
    public static <T> T assertOptEquals(final String message, final T expected, final Optional<T> opt) {
        assertPresent(message, opt);
        assertEquals(message, expected, opt.get());
        return opt.get();
    }

    public static void assertNotThrows(final ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (final Throwable ex) {
            ex.printStackTrace();
            fail("No exception was expected: %s".formatted(ex.getMessage()));
        }
    }

    public static void assertThrows(final ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (final Throwable $) {
            return;
        }

        fail("Expected an exception to be thrown but nothing was thrown.");
    }

    /**
     * Like {@link Assert#assertThrows(Class, ThrowingRunnable)} but allows futher processing of the thrown exception.
     */
    public static <T extends Throwable> void assertThrows(
            final ThrowingRunnable runnable, final Class<T> throwableType, final Consumer<? super T> action)
    {
        final T throwable = Assert.assertThrows(throwableType, runnable);
        action.accept(throwable);
    }

}
