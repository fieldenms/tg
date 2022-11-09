package ua.com.fielden.platform.processors.test_utils;

import java.util.Locale;

import javax.tools.Diagnostic;

/**
 * A diagnostic collector that prints messages to stdout.
 *
 * @author TG Team
 */
public class PrintingDiagnosticCollector<S> extends DiagnosticCollector<S> {

    @Override
    public void report(final Diagnostic<? extends S> diagnostic) {
        super.report(diagnostic);
        System.out.println(diagnostic.getMessage(Locale.getDefault()));
    }

}
