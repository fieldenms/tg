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
        return of(namedImport("TgReflector", "/app/tg-reflector"));
    }

    @Deprecated(since = WARN_DEPRECATION_DANGEROUS_CODE_CONCATENATION_WITHOUT_IMPORTS)
    @Override
    public JsCode build() {
        return jsCode("""
            const reflector = new TgReflector();
            let entity = null;
            if (action.requireSelectedEntities === 'ONE') {
                entity = action.currentEntity();
            } else if (action.requireSelectedEntities === 'ALL' && self.$.egi.getSelectedEntities().length > 0) {
                entity = self.$.egi.getSelectedEntities()[0];
            } else if (action.requireMasterEntity === "true") {
                if (%s) {
                    entity = action.parentElement.entity['@@origin'];
                } else {
                    const value = reflector.tg_getFullValue(action.parentElement.entity, action.parentElement.propertyName);
                    entity = reflector.isEntity(value) ? value : action.parentElement.entity['@@origin'];
                }
            }
            if (entity) {
                action.shortDesc = reflector.getType(entity.constructor.prototype.type.call(entity).notEnhancedFullClassName()).entityTitle();
            }
        """.formatted(
            useMasterEntity
        ));
    }

}
