package ua.com.fielden.platform.web.ref_hierarchy;

import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchy;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelectorDone;
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

    public static final String ERR_CONTEXT_IS_INCORRECT = "Reference Hierarchy action context should contain at least one of the following: computation function, selected entities, current entity or master entity.";

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
            .preAction(new ReferenceHierarchyPreAction(false))
            .icon("tg-reference-hierarchy:hierarchy")
            .shortDesc("Reference Hierarchy")
            .longDesc("Opens Reference Hierarchy")
            .withNoParentCentreRefresh()
            .build();
    }

    /**
     * Produces a new reference hierarchy action configuration as property action for master.
     * This action will open reference hierarchy for master entity if property is not of an entity type,
     * otherwise reference hierarchy will be opened for entity value specified in the entity editor.
     *
     * @return
     */
    public static EntityActionConfig mkPropAction() {
        return action(ReferenceHierarchy.class)
                .withContext(context().withMasterEntity().build())
                .preAction(new ReferenceHierarchyPreAction(false))
                .icon("tg-reference-hierarchy:hierarchy")
                .shortDesc("Reference Hierarchy")
                .longDesc("Opens Reference Hierarchy")
                .withNoParentCentreRefresh()
                .build();
    }

    /**
     * Produces a new reference hierarchy action configuration as property action for master which opens reference hierarchy for master entity.
     *
     * @return
     */
    public static EntityActionConfig mkPropActionForMasterEntity() {
        final CentreContextConfig contextConfig = context().withMasterEntity().withComputation((entity, context) -> context.getMasterEntity()).build();
        return action(ReferenceHierarchy.class)
                .withContext(contextConfig)
                .preAction(new ReferenceHierarchyPreAction(true))
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
        if (!ccConfig.withAllSelectedEntities && !ccConfig.withCurrentEtity && !ccConfig.withMasterEntity && !ccConfig.computation.isPresent()) {
            throw failuref(ERR_CONTEXT_IS_INCORRECT);
        }
        return action(ReferenceHierarchy.class)
            .withContext(ccConfig)
            .preAction(new ReferenceHierarchyPreAction(false))
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

        private final boolean useMasterEntity;

        public ReferenceHierarchyPreAction(final boolean useMasterEntity) {
            this.useMasterEntity = useMasterEntity;
        }
        @Override
        public JsCode build() {
            return new JsCode(String.format(
                    """
                    const reflector = new TgReflector();
                    let entity = null;
                    if (action.requireSelectedEntities === 'ONE') {
                        entity = action.currentEntity();
                    } else if (action.requireSelectedEntities === 'ALL' && self.$.egi.getSelectedEntities().length > 0) {
                        entity = self.$.egi.getSelectedEntities()[0];
                    } else if (action.requireMasterEntity === "true") {
                        if(%s) {
                            entity = action.parentElement.entity['@@origin'];
                        } else {
                            const value = reflector.tg_getFullValue(action.parentElement.entity, action.parentElement.propertyName);
                            entity = reflector.isEntity(value) ? value : action.parentElement.entity['@@origin'];
                        }
                    }
                    if (entity) {
                        action.shortDesc = reflector.getType(entity.constructor.prototype.type.call(entity).notEnhancedFullClassName()).entityTitle();
                    }
                    """, this.useMasterEntity));
        }

    }

}
