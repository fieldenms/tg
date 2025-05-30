package ua.com.fielden.platform.web.ref_hierarchy;

import com.google.inject.Injector;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchy;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;
import ua.com.fielden.platform.web.view.master.hierarchy.ReferenceHierarchyMaster;

import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.minijs.JsCode.jsCode;

/**
 * Web UI configuration for reference hierarchy master and action needed to call reference hierarchy.
 *
 * @author TG Team
 *
 */
public class ReferenceHierarchyWebUiConfig {

    public static final String ERR_CONTEXT_IS_INCORRECT = "Reference Hierarchy action context should have at least one of the following: computation function, selected entities, current entity or master entity.";

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
     * Produces a new reference hierarchy action configuration as property action for an entity master.
     * This action opens the reference hierarchy for the master entity if the property is not of an entity type.
     * Otherwise, the reference hierarchy is opened for the entity value in the property.
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
     * Produces a new reference hierarchy action configuration as a property action for an entity master, which opens reference hierarchy for the master entity.
     * If the master entity is activatable and the action is associated with property `active` then action's `activeOnly` is set to `true`.
     */
    public static EntityActionConfig mkPropActionForMasterEntity() {
        final CentreContextConfig contextConfig = context().withMasterEntity().withComputation((action, context) -> {
            // If the reference hierarchy action is associated with property `active` of an activatable entity,
            // then `activeOnly` should be defaulted to true;
            if (context.getChosenProperty() instanceof String chosenPropName && ActivatableAbstractEntity.ACTIVE.equals(chosenPropName)) {
                final Class<?> propType = determinePropertyType(context.getMasterEntity().getType(), chosenPropName);
                if (propType != null && !ActivatableAbstractEntity.class.isAssignableFrom(propType)) {
                    action.set("activeOnly", true);
                }
            }
            return context.getMasterEntity();
        }).build();
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
     * Produces a new reference hierarchy action configuration with custom context.
     * The context should not be empty.
     */
    public static EntityActionConfig mkAction(final CentreContextConfig ccConfig) {
        if (!ccConfig.withAllSelectedEntities && !ccConfig.withCurrentEtity && !ccConfig.withMasterEntity && ccConfig.computation.isEmpty()) {
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
     */
    private record ReferenceHierarchyPreAction(boolean useMasterEntity) implements IPreAction {
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
                          """.formatted(this.useMasterEntity));
        }
    }

}
