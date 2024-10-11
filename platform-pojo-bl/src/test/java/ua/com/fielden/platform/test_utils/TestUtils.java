package ua.com.fielden.platform.test_utils;

import org.junit.Assert;
import org.junit.function.ThrowingRunnable;

import java.util.Collection;
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
     * Asserts that an optional is empty.
     *
     * @return the given optinal
     * @see #assertEmpty(String, Optional)
     */
    public static <T> Optional<T> assertEmpty(final Optional<T> opt) {
        return assertEmpty("Optional is not empty.", opt);
    }

    /**
     * Asserts that an optional is empty.
     *
     * @return the given optinal
     */
    public static <T> Optional<T> assertEmpty(final String message, final Optional<T> opt) {
        assertTrue(message, opt.isEmpty());
        return opt;
    }

    /**
     * Asserts that an optional is present and its value is equal to the expected one.
     *
     * @return  value described by the Optional
     */
    public static <T> T assertOptEquals(final T expected, final Optional<? extends T> opt) {
        final T actual = assertPresent(opt);
        assertEquals(expected, actual);
        return actual;
    }

    /**
     * Asserts that an optional is present and its value is equal to the expected one.
     *
     * @return  value described by the Optional
     */
    public static <T> T assertOptEquals(final String message, final T expected, final Optional<T> opt) {
        final T actual = assertPresent(message, opt);
        assertEquals(message, expected, actual);
        return actual;
    }

    public static <T> T assertInstanceOf(final Class<T> type, final Object object) {
        if (type.isInstance(object)) {
            return type.cast(object);
        }
        throw new AssertionError("Expected [%s] but was: %s".formatted(type.getTypeName(), object));
    }

    public static void assertNotThrows(final ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (final Throwable ex) {
            ex.printStackTrace();
            fail("No exception was expected: %s".formatted(ex.getMessage()));
        }
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

    public static <E, C extends Collection<E>> C assertNotEmpty(final C collection) {
        if (collection.isEmpty()) {
            fail("Collection is empty.");
        }
        return collection;
    }

    public static <E, C extends Collection<E>> C assertNotEmpty(final String message, final C collection) {
        if (collection.isEmpty()) {
            fail(message);
        }
        return collection;
    }

    public static <E, C extends Collection<E>> C assertEmpty(final C collection) {
        if (!collection.isEmpty()) {
            fail("Collection is not empty.");
        }
        return collection;
    }

    public static <E, C extends Collection<E>> C assertEmpty(final String message, final C collection) {
        if (!collection.isEmpty()) {
            fail(message);
        }
        return collection;
    }

}
