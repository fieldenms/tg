package ua.com.fielden.platform.processors.test_utils;

import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.Collection;

import static org.junit.Assert.fail;

/**
 * General-purpose test utilities for operating on {@link Collection}s.
 *
 * @author TG Team
 */
public class CollectionTestUtils {

    private CollectionTestUtils() {}

    /**
     * Asserts that two collections have the same contents irrespective of order.
     *
     * @param c1    the first collection
     * @param c2    the second collection
     */
    public static void assertEqualByContents(final Collection<?> c1, final Collection<?> c2) {
        assertEqualByContents(null, c1, c2);
    }

    /**
     * Asserts that two collections have the same contents irrespective of order.
     *
     * @param message   the identifying message for the {@link AssertionError}
     * @param c1        the first collection
     * @param c2        the second collection
     */
    public static void assertEqualByContents(final String message, final Collection<?> c1, final Collection<?> c2) {
        if (!CollectionUtil.areEqualByContents(c1, c2)) {
            fail("%sExpected:[%s] but was:[%s]".formatted(message == null ? "" : message + " ",
                    CollectionUtil.toString(c1, ", "), CollectionUtil.toString(c2, ", ")));
        }
    }

}
