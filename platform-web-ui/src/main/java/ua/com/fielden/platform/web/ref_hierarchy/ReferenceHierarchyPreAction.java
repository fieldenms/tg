package ua.com.fielden.platform.web.ref_hierarchy;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.minijs.JsImport;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

import java.util.Set;

import static java.util.Set.of;
import static ua.com.fielden.platform.web.minijs.JsCode.jsCode;
import static ua.com.fielden.platform.web.minijs.JsImport.namedImport;

/// Common [IPreAction] for [ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchy] action.
///
/// @author TG Team
public record ReferenceHierarchyPreAction(boolean useMasterEntity) implements IPreAction {

    @Override
    public Set<JsImport> importStatements() {
        return of(namedImport("referenceHierarchy", "centre/actions/tg-reference-hierarchy"));
    }

    @Deprecated(since = WARN_DEPRECATION_DANGEROUS_CODE_CONCATENATION_WITHOUT_IMPORTS)
    @Override
    public JsCode build() {
        return jsCode("""
            referenceHierarchy(action, self, %s);
        """.formatted(
            useMasterEntity
        ));
    }

}
