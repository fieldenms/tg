package ua.com.fielden.platform.processors.test_utils;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Locale;

import javax.tools.Diagnostic.Kind;

/**
 * A collection of test utilities to assist in working with {@link Compilation} and {@link CompilationResult}.
 *
 * @author TG Team
 */
public class CompilationTestUtils {

    private CompilationTestUtils() {}

    /**
     * Asserts that diagnostic messages of a given kind were reported as a result of a compilation.
     *
     * @param result    represents compilation results
     * @param kind      the message kind
     * @param messages  the messages, existence of which is to be asserted
     */
    public static void assertMessages(final CompilationResult result, final Kind kind, final String... messages) {
        Arrays.stream(messages).forEach(msg -> assertMessage(result, kind, msg));
    }

    private static void assertMessage(final CompilationResult result, final Kind kind, final String message) {
        assertTrueOrFailWith("No %s was reported with message \"%s\"".formatted(kind, message),
                result.diagnosticsByKind(kind).stream().anyMatch(diag -> diag.getMessage(Locale.getDefault()).equals(message)),
                () -> result.printDiagnostics());
    }

    public static void assertTrueOrFailWith(final String message, boolean condition, final Runnable failAction) {
        if (!condition) {
            failAction.run();
            fail(message);
        }
    }

}
