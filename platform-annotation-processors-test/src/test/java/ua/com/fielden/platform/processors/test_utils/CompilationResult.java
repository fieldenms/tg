package ua.com.fielden.platform.processors.test_utils;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.processors.test_utils.exceptions.CompilationException;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/// Represents results of a compilation: status (success/failure) and collected diagnostics.
///
public final class CompilationResult {

    private final boolean success;
    private final List<Diagnostic<? extends JavaFileObject>> diagnostics;
    private final List<? extends JavaFileObject> generatedSources;
    private final List<ClassFile> outputClasses;

    CompilationResult(
            final boolean success,
            final List<Diagnostic<? extends JavaFileObject>> diagnostics,
            final Collection<? extends JavaFileObject> generatedSources,
            final List<ClassFile> outputClasses)
    {
        this.success = success;
        this.diagnostics = new ArrayList<>(diagnostics);
        this.generatedSources = List.copyOf(generatedSources);
        this.outputClasses = ImmutableList.copyOf(outputClasses);
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

    public List<ClassFile> outputClasses() {
        return outputClasses;
    }

    /// Returns all diagnostics.
    ///
    /// @return a list view of all diagnostics
    ///
    public List<Diagnostic<? extends JavaFileObject>> diagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }

    /// Returns a grouping of all diagnostics.
    /// The returned map contains an entry for each diagnostic kind.
    ///
    public Map<Kind, List<Diagnostic<? extends JavaFileObject>>> groupDiagnostics() {
        final var groups = diagnostics.stream()
                .collect(Collectors.groupingBy(Diagnostic::getKind, HashMap::new, toList()));
        for (final Kind kind : Kind.values()) {
            if (!groups.containsKey(kind)) {
                groups.put(kind, ImmutableList.of());
            }
        }
        return unmodifiableMap(groups);
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

    /// A convenient method that prints collected diagnostics to [#out].
    ///
    public void printDiagnostics() {
        System.out.println(diagnostics.stream().map(Diagnostic::toString).collect(joining("\n")));
    }

    /// If this compilation result is unsuccessful, throws a runtime exception, using the specified function to create an error message.
    /// Otherwise, returns this instance.
    ///
    /// @param errMsgFn  function that creates an error message; its first parameter is a message produced by the compiler.
    ///
    public CompilationResult throwIfFailed(final Function<String, String> errMsgFn) {
        if (success) {
            return this;
        }
        else {
            final var sb = new StringBuilder();
            final var diagnosticGroups = groupDiagnostics();
            final var allWarningsCount = diagnosticGroups.get(Kind.WARNING).size() + diagnosticGroups.get(Kind.MANDATORY_WARNING).size();
            sb.append("Compilation failed: %s errors, %s warnings, %s infos, %s others\n".formatted(
                    diagnosticGroups.get(Kind.ERROR).size(),
                    allWarningsCount,
                    diagnosticGroups.get(Kind.NOTE).size(),
                    diagnosticGroups.get(Kind.OTHER).size()));
            diagnosticGroups.get(Kind.ERROR).forEach(d -> {
                sb.append("Error: ");
                formatDiagnostic(d, sb);
                sb.append('\n');
            });
            diagnosticGroups.get(Kind.WARNING).forEach(d -> {
                sb.append("Warning: ");
                formatDiagnostic(d, sb);
                sb.append('\n');
            });
            diagnosticGroups.get(Kind.MANDATORY_WARNING).forEach(d -> {
                sb.append("Mandatory warning: ");
                formatDiagnostic(d, sb);
                sb.append('\n');
            });
            diagnosticGroups.get(Kind.NOTE).forEach(d -> {
                sb.append("Note: ");
                formatDiagnostic(d, sb);
                sb.append('\n');
            });
            diagnosticGroups.get(Kind.OTHER).forEach(d -> {
                sb.append("Other: ");
                formatDiagnostic(d, sb);
                sb.append('\n');
            });

            throw new CompilationException(errMsgFn.apply(sb.toString()));
        }
    }

    private static void formatDiagnostic(final Diagnostic<? extends JavaFileObject> diagnostic, final StringBuilder sink) {
        if (diagnostic.getSource() != null) {
            sink.append(diagnostic.getSource().getName());
            final var lineNr = diagnostic.getLineNumber();
            if (lineNr != Diagnostic.NOPOS) {
                sink.append(':').append(lineNr).append(": ");
            }
        }

        sink.append(diagnostic.getMessage(Locale.getDefault()));
    }

    /// @param name            binary name of the class
    /// @param javaFileObject  [JavaFileObject] representing the class
    ///
    public record ClassFile(String name, JavaFileObject javaFileObject) {

        public byte[] getBytes() {
            try (final var is = javaFileObject.openInputStream()) {
                return is.readAllBytes();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
