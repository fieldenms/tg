package ua.com.fielden.platform.web.minijs;

import ua.com.fielden.platform.web.minijs.exceptions.JsCodeException;

import java.util.Comparator;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/// An abstraction for JavaScript imports covering named imports (including aliased) and default imports.
///
/// @author TG Team
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
        if (DEFAULT.equals(name) && alias.isEmpty()) {
            throw new JsCodeException(ERR_JAVA_SCRIPT_DEFAULT_IMPORT_ALIAS_IS_NOT_PROVIDED);
        }
        if (alias.isPresent() && alias.get().isBlank()) {
            throw new JsCodeException(ERR_JAVA_SCRIPT_IMPORT_ALIAS_IS_BLANK.formatted(alias));
        }
    }

    /// Creates a named [JsImport] importing concrete `name` from a `path` module.
    public static JsImport namedImport(final String name, final String path) {
        return new JsImport(name, path, empty());
    }

    /// Creates a named [JsImport] importing concrete `name` with an `alias` from a `path` module.
    public static JsImport namedImport(final String name, final String path, final String alias) {
        return new JsImport(name, path, of(alias));
    }

    /// Creates default [JsImport] with an `alias` from a `path` module.
    public static JsImport defaultImport(final String alias, final String path) {
        return new JsImport("default", path, of(alias));
    }

    /// Converts import statement to single aliased form being able to analyse in a relation to others:
    /// `import{exportedName|default as exportedName|myIdentifier}from 'path';`
    public JsImport convertToAliasedForm() {
        return namedImport(name, path, alias().orElse(name()));
    }

    /// Generates [String] JavaScript code, assuming that this [JsImport] is in aliased form ([#alias()] is present).
    public String genAliasedCode() {
        return "import { %s as %s } from '%s.js';".formatted(
            name(),
            alias().get(), // always present
            (path().startsWith("/") ? "" : "/resources/") + path() // support "full" and short paths ('/app/...' vs 'reflection/...')
        );
    }

    @Override
    public int compareTo(final JsImport other) {
        return ALIASED_FORM_COMPARATOR.compare(this, other);
    }

}
