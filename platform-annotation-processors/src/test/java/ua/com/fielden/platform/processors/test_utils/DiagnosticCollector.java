package ua.com.fielden.platform.processors.test_utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;

/**
 * Provides an easy way to collect diagnostics in a list. This class is mimicking {@link javax.tools.DiagnosticCollector},
 * while being extendable (non-final).
 *
 * @param <S> the type of source objects used by diagnostics received by this object
 * @author TG Team
 */
public class DiagnosticCollector<S> implements DiagnosticListener<S> {
    private List<Diagnostic<? extends S>> diagnostics = Collections.synchronizedList(new ArrayList<Diagnostic<? extends S>>());

    /**
     * Creates a new instance of DiagnosticCollector.
     */
    public DiagnosticCollector() {}

    public void report(final Diagnostic<? extends S> diagnostic) {
        Objects.requireNonNull(diagnostic);
        diagnostics.add(diagnostic);
    }

    /**
     * Returns a list view of diagnostics collected by this object.
     *
     * @return a list view of diagnostics
     */
    public List<Diagnostic<? extends S>> getDiagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }
}