package ua.com.fielden.platform.test_utils;
import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

    public static File assertFileExists(final File file) {
        return assertFileExists(null, file);
    }

    public static File assertFileExists(final String message, final File file) {
        if (!file.exists()) {
            fail((message == null ? "" : (message + ". "))
                 + "File doesn't exist: %s".formatted(file.getAbsolutePath()));
        }
        return file;
    }

    public static Path assertFileExists(final Path path) {
        assertFileExists(null, path.toFile());
        return path;
    }

    public static Path assertFileExists(final String message, final Path path) {
        assertFileExists(message, path.toFile());
        return path;
    }

    public static File assertFileReadable(final File file) {
        return assertFileReadable(null, file);
    }

    public static File assertFileReadable(final String message, final File file) {
        assertFileExists(file);
        if (!file.canRead()) {
            fail((message == null ? "" : (message + ". "))
                 + "File is not readable: %s".formatted(file.getAbsolutePath()));
        }
        return file;
    }

    public static Path assertFileReadable(final Path path) {
        assertFileReadable(null, path.toFile());
        return path;
    }

    public static Path assertFileReadable(final String message, final Path path) {
        assertFileReadable(message, path.toFile());
        return path;
    }

}
