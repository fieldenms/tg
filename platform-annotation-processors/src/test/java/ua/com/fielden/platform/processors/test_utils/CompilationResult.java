package ua.com.fielden.platform.processors.test_utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

/**
 * Represents results of a compilation: status (success/failure) and collected diagnostics.
 *
 * @author homedirectory
 */
public final class CompilationResult {

    private final boolean success;
    private final List<Diagnostic<? extends JavaFileObject>> diagnostics;
    private final List<Throwable> processingErrors;

    CompilationResult(final boolean success, final List<Diagnostic<? extends JavaFileObject>> diagnostics, final List<Throwable> processingErrors) {
        this.success = success;
        this.diagnostics = new LinkedList<>(diagnostics);
        this.processingErrors = new ArrayList<>(processingErrors);
    }

    public boolean success() {
        return success;
    }

    public boolean failure() {
        return !success;
    }

    public List<Throwable> processingErrors() {
        return Collections.unmodifiableList(processingErrors);
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
        System.out.println(diagnostics.stream().map(Diagnostic::toString).collect(Collectors.joining("\n")));
    }

}