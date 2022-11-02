package ua.com.fielden.platform.processors.verify.verifiers;

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

}
