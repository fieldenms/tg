package ua.com.fielden.platform.processors.test_utils;

import static org.junit.Assert.assertTrue;

import java.util.Optional;

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

}
