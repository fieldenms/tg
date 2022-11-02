package ua.com.fielden.platform.processors.verify.verifiers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import ua.com.fielden.platform.processors.test_utils.Compilation;

public class VerifierTestUtils {

    /**
     * A convenient method that replaces 2 repetitive lines of code.
     * @param compilation
     * @return
     * @throws Throwable
     */
    public static boolean compileAndPrintDiagnostics(final Compilation compilation) throws Throwable {
        final boolean success = compilation.compile();
        compilation.printDiagnostics();
        return success;
    }

    /**
     * Asserts that an error containing {@code msg} was reported during compilation.
     * @param compilation
     * @param msg
     */
    public static void assertErrorReported(final Compilation compilation, final String msg) {
        final List<Diagnostic<? extends JavaFileObject>> errors = compilation.getErrors();
        assertFalse("An error should have been reported.", errors.isEmpty());
        assertTrue("No error with a matching message was reported.",
                compilation.getErrors().stream().anyMatch(err -> msg.equals(err.getMessage(Locale.getDefault()))));
    }

}
