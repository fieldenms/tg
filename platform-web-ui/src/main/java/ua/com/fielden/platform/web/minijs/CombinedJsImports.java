package ua.com.fielden.platform.web.minijs;

import ua.com.fielden.platform.web.minijs.exceptions.JsCodeException;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.lang.String.join;
import static java.util.stream.Collectors.toSet;
import static org.apache.tika.utils.StringUtils.isBlank;

/// A [SortedSet] of [JsImport]s with name conflict validation and ability to convert to code [String]s.
///
/// @author TG Team
public class CombinedJsImports extends TreeSet<JsImport> {
    private static final String ERR_ACTION_IMPORT_NAMES_ARE_IN_CONFLICT = "Action import names are in conflict.\n%s";

    /// Overridden to validate [CombinedJsImports] on naming conflicts.
    ///
    /// Some `jsImports` can be aliased and others - not.
    /// To validate on naming conflicts, all `jsImports` should be converted to aliased form and only then added to set.
    /// Sorting will be done using [JsImport#ALIASED_FORM_COMPARATOR].
    @Override
    public boolean addAll(final Collection<? extends JsImport> jsImports) {
        final var changed = super.addAll(jsImports.stream().map(JsImport::convertToAliasedForm).collect(toSet()));
        if (stream().map(JsImport::alias).distinct().toList().size() < size()) {
            throw new JsCodeException(ERR_ACTION_IMPORT_NAMES_ARE_IN_CONFLICT.formatted(this));
        }
        return changed;
    }

    /// Overridden to return JavaScript code representation (in [String] form).
    @Override
    public String toString() {
        if (isEmpty()) {
            return "";
        }
        return "\n" + join("\n", stream().map(JsImport::genAliasedCode).toList());
    }

    /// Overridden to return JavaScript code representation (in [String] form).
    ///
    /// @param importObjectName a name for special JS object containing `importedFunction: importedFunction` pairs;
    ///     these pairs can be referenced in dynamic JavaScript functions (`new Function(" some JS code ")`)
    public String toStringWith(final String importObjectName) {
        final var codeStr = toString();
        if (isBlank(codeStr)) {
            return "";
        }
        return codeStr + "\nconst %s = {%s};".formatted(
            importObjectName,
            join(",", stream()
                .map(jsImport -> jsImport.alias().get()) // always present
                .map(alias -> "%s: %s".formatted(alias, alias))
                .toList()
            )
        );
    }

}
