package ua.com.fielden.platform.test_utils;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A collection of general-purpose test utilities.
 *
 * @author TG Team
 */
public class TestUtils {

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
    public static <T> T assertOptEquals(final T expected, final Optional<T> opt) {
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

}
