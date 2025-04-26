package ua.com.fielden.platform.web.action.pre;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.minijs.JsImport;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

import java.util.Set;

import static java.util.Set.of;
import static ua.com.fielden.platform.web.minijs.JsCode.jsCode;
import static ua.com.fielden.platform.web.minijs.JsImport.namedImport;

/// This pre-action implementation should be used only with sequential edit action.
///
/// @author TG Team
public class SequentialEditPreAction implements IPreAction {

    /// Creates [SequentialEditPreAction].
    SequentialEditPreAction() {}

    @Override
    public Set<JsImport> importStatements() {
        return of(namedImport("sequentialEdit", "centre/actions/tg-sequential-edit"));
    }

    @Deprecated(since = WARN_DEPRECATION_DANGEROUS_CODE_CONCATENATION_WITHOUT_IMPORTS)
    @Override
    public JsCode build() {
        return jsCode("""
            sequentialEdit(action, self);
        """);
    }

}
