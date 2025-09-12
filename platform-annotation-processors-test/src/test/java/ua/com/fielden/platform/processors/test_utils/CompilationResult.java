package ua.com.fielden.platform.processors.test_utils;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * Represents results of a compilation: status (success/failure) and collected diagnostics.
 *
 * @author TG Team
 */
public final class CompilationResult {

    private final boolean success;
    private final List<Diagnostic<? extends JavaFileObject>> diagnostics;
    private final List<? extends JavaFileObject> generatedSources;

    CompilationResult(
            final boolean success,
            final List<Diagnostic<? extends JavaFileObject>> diagnostics,
            final Collection<? extends JavaFileObject> generatedSources)
    {
        this.success = success;
        this.diagnostics = new ArrayList<>(diagnostics);
        this.generatedSources = List.copyOf(generatedSources);
    }

    public boolean success() {
        return success;
    }

    public boolean failure() {
        return !success;
    }

    public List<? extends JavaFileObject> generatedSources() {
        return generatedSources;
    }

    /**
     * Returns all diagnostics.
     *
     * @return a list view of all diagnostics
     */
    public List<Diagnostic<? extends JavaFileObject>> diagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }

    public List<Diagnostic<? extends JavaFileObject>> diagnosticsByKind(final Kind kind) {
        return diagnostics.stream()
                .filter(diag -> diag.getKind().equals(kind))
                .toList();
    }

    public List<Diagnostic<? extends JavaFileObject>> errors() {
        return diagnosticsByKind(Kind.ERROR);
    }

    public List<Diagnostic<? extends JavaFileObject>> warnings() {
        return diagnosticsByKind(Kind.WARNING);
    }

    public List<Diagnostic<? extends JavaFileObject>> mandatoryWarnings() {
        return diagnosticsByKind(Kind.MANDATORY_WARNING);
    }

    public List<Diagnostic<? extends JavaFileObject>> notes() {
        return diagnosticsByKind(Kind.NOTE);
    }

    public List<Diagnostic<? extends JavaFileObject>> other() {
        return diagnosticsByKind(Kind.OTHER);
    }

    /** A convenient method that prints collected diagnostics to {@link System#out}. */
    public void printDiagnostics() {
        System.out.println(diagnostics.stream().map(Diagnostic::toString).collect(joining("\n")));
    }

}
