package ua.com.fielden.platform.web.minijs;

import ua.com.fielden.platform.web.minijs.exceptions.JsCodeException;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

import static java.lang.String.join;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toSet;

/**
 * An abstraction for JavaScript imports covering named imports (including aliased) and default imports.
 *
 * @author TG Team
 *
 */
public record JsImport(String name, String path, Optional<String> alias) implements Comparable<JsImport> {
    private static final String DEFAULT = "default"; // reserved JavaScript word for default imports
    private static final String ERR_JAVA_SCRIPT_IMPORT_NAME_IS_BLANK = "JavaScript import statement name [%s] is blank. Should either be [%s] or actual named export identifier from [%s] module.";
    private static final String ERR_JAVA_SCRIPT_IMPORT_PATH_IS_BLANK = "JavaScript import statement path [%s] is blank.";
    public static final String ERR_JAVA_SCRIPT_DEFAULT_IMPORT_ALIAS_IS_NOT_PROVIDED = "JavaScript default import statement alias is not provided.";
    public static final String ERR_JAVA_SCRIPT_IMPORT_ALIAS_IS_BLANK = "JavaScript import statement alias [%s] is blank.";

    private static final Comparator<JsImport> ALIASED_FORM_COMPARATOR =
        comparing(JsImport::path)
        .thenComparing(jsImport -> jsImport.alias().get());

    public JsImport {
        if (requireNonNull(name).isBlank()) {
            throw new JsCodeException(ERR_JAVA_SCRIPT_IMPORT_NAME_IS_BLANK.formatted(name, DEFAULT, path));
        }
        if (requireNonNull(path).isBlank()) {
            throw new JsCodeException(ERR_JAVA_SCRIPT_IMPORT_PATH_IS_BLANK.formatted(path));
        }
        if (DEFAULT.equals(name) && alias.isPresent()) {
            throw new JsCodeException(ERR_JAVA_SCRIPT_DEFAULT_IMPORT_ALIAS_IS_NOT_PROVIDED);
        }
        if (alias.isPresent() && alias.get().isBlank()) {
            throw new JsCodeException(ERR_JAVA_SCRIPT_IMPORT_ALIAS_IS_BLANK.formatted(alias));
        }
    }

    /**
     * Creates a named {@link JsImport} importing concrete {@code name} from a {@code path} module.
     */
    public static JsImport namedImport(final String name, final String path) {
        return new JsImport(name, path, empty());
    }

    /**
     * Creates a named {@link JsImport} importing concrete {@code name} with an {@code alias} from a {@code path} module.
     */
    public static JsImport namedImport(final String name, final String path, final String alias) {
        return new JsImport(name, path, of(alias));
    }

    /**
     * Creates default {@link JsImport} with an {@code alias} from a {@code path} module.
     */
    public static JsImport defaultImport(final String alias, final String path) {
        return new JsImport("default", path, of(alias));
    }

    /**
     * Converts import statement to single aliased form being able to analyse in a relation to others:<br>
     * {@code import {exportedName|default as exportedName|myIdentifier} from 'path';}
     */
    public JsImport convertToAliasedForm() {
        return namedImport(name, path, alias().orElse(name()));
    }

    @Override
    public int compareTo(final JsImport other) {
        return ALIASED_FORM_COMPARATOR.compare(this, other);
    }

    private static Set<JsImport> convertJsImportsToAliasedForm(final Set<JsImport> jsImports) {
        return jsImports.stream().map(JsImport::convertToAliasedForm).collect(toSet());
    }

    public static SortedSet<JsImport> extendAndValidateCombinedImports(final SortedSet<JsImport> combinedImports, final Set<JsImport> jsImports) {
        combinedImports.addAll(convertJsImportsToAliasedForm(jsImports));
        if (combinedImports.stream().map(JsImport::alias).distinct().toList().size() < combinedImports.size()) {
            throw new JsCodeException("Action import names are in conflict.\n%s".formatted(combinedImports));
        }
        return combinedImports;
    }

    public static String extractImportStatements(final SortedSet<JsImport> combinedImports, final Optional<String> importObjectNameOpt) {
        return combinedImports.isEmpty() ? ""
            : "\n" + join("\n", combinedImports.stream().map(jsImport -> "import { %s as %s } from '%s.js';".formatted(jsImport.name(), jsImport.alias().get(), (jsImport.path().startsWith("/") ? "" : "/resources/") + jsImport.path())).toList())
            + importObjectNameOpt.map(importObjectName ->
                "\n" + "const %s = {%s};".formatted(importObjectName, join(",", combinedImports.stream().map(jsImport -> jsImport.alias().get()).map(alias -> "%s: %s".formatted(alias, alias)).toList()))
            ).orElse("");
    }

}
