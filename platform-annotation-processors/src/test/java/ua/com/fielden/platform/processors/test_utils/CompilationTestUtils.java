package ua.com.fielden.platform.processors.test_utils;

import javax.tools.Diagnostic.Kind;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.fail;

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

    public static void assertTrueOrFailWith(final String message, final boolean condition, final Runnable failAction) {
        if (!condition) {
            failAction.run();
            fail(message);
        }
    }

    /**
     * Asserts the success of a compilation. In case of an unsuccessful compilation, all diagnostic messages are printed to standard output.
     *
     * @param result    compilation results
     */
    public static void assertSuccess(final CompilationResult result) {
        assertTrueOrFailWith("Compilation failed.", result.success(), () -> result.printDiagnostics());
    }

    /**
     * Asserts the absence of errors raised (i.e., exceptions thrown) during annotation processing.
     * In case of a failed assertion, all diagnostic messages are printed to standard output and the first raised error is rethrown.
     * <p>
     * Note that presence of such errors does not determine the compilation's success.
     *
     * @param result    compilation results
     */
    public static void assertNoProcessingErrors(final CompilationResult result) {
        final List<Throwable> processingErrors = result.processingErrors();
        if (!processingErrors.isEmpty()) {
            result.printDiagnostics();
            // rethrow the 1st processing error
            throw new AssertionError(
                    "There were %s processing errors. See the cause for the first one.".formatted(processingErrors.size()),
                    processingErrors.get(0));
        }
    }

    /**
     * Combines {@link #assertSuccess(CompilationResult)} and {@link #assertNoProcessingErrors(CompilationResult)}.
     * @param result
     */
    public static void assertSuccessWithoutProcessingErrors(final CompilationResult result) {
        assertSuccess(result);
        assertNoProcessingErrors(result);
    }

}
