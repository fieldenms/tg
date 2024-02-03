package ua.com.fielden.platform.web.ref_hierarchy;

import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;

import com.google.inject.Injector;

import ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchy;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;
import ua.com.fielden.platform.web.view.master.hierarchy.ReferenceHierarchyMaster;

/**
 * Web UI configuration for reference hierarchy master and action needed to call reference hierarchy.
 *
 * @author TG Team
 *
 */
public class ReferenceHierarchyWebUiConfig {

    /**
     * Creates the reference hierarchy master
     *
     * @param injector
     * @return
     */
    public static EntityMaster<ReferenceHierarchy> createReferenceHierarchyMaster(final Injector injector) {
        return new EntityMaster<>(ReferenceHierarchy.class,
                ReferenceHierarchyProducer.class,
                new ReferenceHierarchyMaster(),
                injector);
    }

    /**
     * Produces a new reference hierarchy action configuration as top action for EGI.
     *
     * @return
     */
    public static EntityActionConfig mkAction() {
        return action(ReferenceHierarchy.class)
            .withContext(context().withSelectedEntities().build())
            .preAction(new ReferenceHierarchyPreAction())
            .icon("tg-reference-hierarchy:hierarchy")
            .shortDesc("Reference Hierarchy")
            .longDesc("Opens Reference Hierarchy")
            .withNoParentCentreRefresh()
            .build();
    }

    /**
     * Produces a new reference hierarchy action configuration as with custom context make sure that context has current entity or selected entities.
     *
     * @return
     */
    public static EntityActionConfig mkAction(final CentreContextConfig ccConfig) {
        if (!ccConfig.withAllSelectedEntities && !ccConfig.withCurrentEtity) {
            throw failuref("Reference Hierarchy action context should contain selected entities or current entity.");
        }
        return action(ReferenceHierarchy.class)
            .withContext(ccConfig)
            .preAction(new ReferenceHierarchyPreAction())
            .icon("tg-reference-hierarchy:hierarchy")
            .shortDesc("Reference Hierarchy")
            .longDesc("Opens Reference Hierarchy")
            .withNoParentCentreRefresh()
            .build();
    }

    /**
     * Common {@link IPreAction} for reference hierarchy action.
     *
     * @author TG Team
     *
     */
    private static class ReferenceHierarchyPreAction implements IPreAction {

        @Override
        public JsCode build() {
            return new JsCode(
                    "const reflector = new TgReflector();\n"
                    + "if (action.requireSelectedEntities === 'ONE') {\n"
                    + "    const entity = action.currentEntity();\n"
                    + "    action.shortDesc = reflector.getType(entity.constructor.prototype.type.call(entity).notEnhancedFullClassName()).entityTitle();\n"
                    + "} else if (action.requireSelectedEntities === 'ALL' && self.$.egi.getSelectedEntities().length > 0) {\n"
                    + "    const entity = self.$.egi.getSelectedEntities()[0];\n"
                    + "    action.shortDesc = reflector.getType(entity.constructor.prototype.type.call(entity).notEnhancedFullClassName()).entityTitle();\n"
                    + "}\n");
        }

    }
}
