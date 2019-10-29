package ua.com.fielden.platform.web.test.server.config;

import static java.lang.String.format;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.web.action.pre.ConfirmationPreAction.okCancel;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.test.server.config.StandardActionsStyles.STANDARD_ACTION_COLOUR;
import static ua.com.fielden.platform.web.test.server.config.StandardMessages.DELETE_CONFIRMATION;

import java.util.Optional;
import java.util.function.BiFunction;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.EntityDeleteAction;
import ua.com.fielden.platform.entity.EntityExportAction;
import ua.com.fielden.platform.entity.EntityNavigationAction;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.web.PrefDim;
import ua.com.fielden.platform.web.action.exceptions.ActionConfigurationException;
import ua.com.fielden.platform.web.action.post.FileSaverPostAction;
import ua.com.fielden.platform.web.action.pre.EntityNavigationPreAction;
import ua.com.fielden.platform.web.action.pre.SequentialEditPreAction;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelectorDone;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

/**
 * Enumeration of standard UI action configurations that can be uniformly used throughout Web UI configuration for different entities.
 *
 * @author TTGAMS Team
 *
 */
public enum StandardActions {

    NEW_ACTION {
        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType) {
            return mkAction(entityType, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final PrefDim prefDim) {
            return mkAction(entityType, Optional.empty(), Optional.of(prefDim), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation) {
            return mkAction(entityType, Optional.of(computation), Optional.empty(), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation, final PrefDim prefDim) {
            return mkAction(entityType, Optional.of(computation), Optional.of(prefDim), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkActionWithIcon(final Class<? extends AbstractEntity<?>> entityType, final String iconName, final Optional<String> iconStyle) {
            return mkAction(entityType, Optional.empty(), Optional.empty(), Optional.of(iconName), iconStyle);
        }

        private EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType,
                final Optional<BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object>> computation,
                final Optional<PrefDim> prefDim,
                final Optional<String> iconName, final Optional<String> iconStyle) {
            final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entityType).getKey();

            final IEntityCentreContextSelectorDone<AbstractEntity<?>> contextConfig = context().withSelectionCrit().withComputation(computation.orElse((entity, context) -> entityType));

            return action(EntityNewAction.class)
                    .withContext(contextConfig.build())
                    .icon(iconName.orElse("icons:add-circle-outline"))
                    .withStyle(iconStyle.orElse(STANDARD_ACTION_COLOUR))
                    .shortDesc(format("Add new %s", entityTitle))
                    .longDesc(format("Add new %s", entityTitle))
                    .shortcut("alt+n")
                    .prefDimForView(prefDim.orElse(null))
                    .withNoParentCentreRefresh()
                    .build();
        }

        @Override
        public EntityActionConfig mkActionWithPreAction(final Class<? extends AbstractEntity<?>> entityType, final IPreAction preAction) {
            throw new UnsupportedOperationException("NEW_ACTION does not support preAction!");
        }
    },

    NEW_WITH_MASTER_ACTION {
        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType) {
            return mkAction(entityType, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final PrefDim prefDim) {
            return mkAction(entityType, Optional.empty(), Optional.of(prefDim), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation) {
            return mkAction(entityType, Optional.of(computation), Optional.empty(), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation, final PrefDim prefDim) {
            return mkAction(entityType, Optional.of(computation), Optional.of(prefDim), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkActionWithIcon(final Class<? extends AbstractEntity<?>> entityType, final String iconName, final Optional<String> iconStyle) {
            return mkAction(entityType, Optional.empty(), Optional.empty(), Optional.of(iconName), iconStyle);
        }

        private EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType,
                final Optional<BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object>> computation,
                final Optional<PrefDim> prefDim,
                final Optional<String> iconName,
                final Optional<String> iconStyle) {
            final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entityType).getKey();

            final IEntityCentreContextSelectorDone<AbstractEntity<?>> contextConfig = context().withMasterEntity().withSelectionCrit().withComputation(computation.orElse((entity, context) -> entityType));

            return action(EntityNewAction.class)
                    .withContext(contextConfig.build())
                    .icon(iconName.orElse("icons:add-circle-outline"))
                    .withStyle(iconStyle.orElse(STANDARD_ACTION_COLOUR))
                    .shortDesc(format("Add new %s", entityTitle))
                    .longDesc(format("Add new %s", entityTitle))
                    .shortcut("alt+n")
                    .prefDimForView(prefDim.orElse(null))
                    .withNoParentCentreRefresh()
                    .build();
        }

        @Override
        public EntityActionConfig mkActionWithPreAction(final Class<? extends AbstractEntity<?>> entityType, final IPreAction preAction) {
            throw new UnsupportedOperationException("NEW_WITH_MASTER_ACTION does not support preAction!");
        }
    },


    EDIT_ACTION {
        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType) {
            return mkAction(entityType, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final PrefDim prefDim) {
            return mkAction(entityType, Optional.empty(), Optional.of(prefDim), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation) {
            return mkAction(entityType, Optional.of(computation), Optional.empty(), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation, final PrefDim prefDim) {
            return mkAction(entityType, Optional.of(computation), Optional.of(prefDim), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkActionWithIcon(final Class<? extends AbstractEntity<?>> entityType, final String iconName, final Optional<String> iconStyle) {
            return mkAction(entityType, Optional.empty(), Optional.empty(), Optional.of(iconName), iconStyle);
        }

        private EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType,
                final Optional<BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object>> computation,
                final Optional<PrefDim> prefDim,
                final Optional<String> iconName, final Optional<String> iconStyle) {
            final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entityType).getKey();

            final IEntityCentreContextSelectorDone<AbstractEntity<?>> contextConfig = context().withCurrentEntity().withSelectionCrit().withComputation(computation.orElse((entity, context) -> entityType));

            return action(EntityNavigationAction.class)
                    .withContext(contextConfig.build())
                    .preAction(new EntityNavigationPreAction(entityTitle))
                    .icon(iconName.orElse("editor:mode-edit"))
                    .withStyle(iconStyle.orElse(STANDARD_ACTION_COLOUR))
                    .shortDesc(format("Edit %s", entityTitle))
                    .longDesc(format("Edit %s", entityTitle))
                    .prefDimForView(prefDim.orElse(null))
                    .withNoParentCentreRefresh()
                    .build();
        }

        @Override
        public EntityActionConfig mkActionWithPreAction(final Class<? extends AbstractEntity<?>> entityType, final IPreAction preAction) {
            throw new UnsupportedOperationException("EDIT_ACTION does not support preAction!");
        }
    },

    SEQUENTIAL_EDIT_ACTION {

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType) {
            return mkAction(entityType, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final PrefDim prefDim) {
            return mkAction(entityType, Optional.empty(), Optional.of(prefDim), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation) {
            return mkAction(entityType, Optional.of(computation), Optional.empty(), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation, final PrefDim prefDim) {
            return mkAction(entityType, Optional.of(computation), Optional.of(prefDim), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkActionWithIcon(final Class<? extends AbstractEntity<?>> entityType, final String iconName, final Optional<String> iconStyle) {
            return mkAction(entityType, Optional.empty(), Optional.empty(), Optional.of(iconName), iconStyle);
        }

        @Override
        public EntityActionConfig mkActionWithPreAction(final Class<? extends AbstractEntity<?>> entityType, final IPreAction preAction) {
            throw new ActionConfigurationException("The sequential edit action can not have custom pre action.");
        }

        private EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType,
                final Optional<BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object>> computation,
                final Optional<PrefDim> prefDim,
                final Optional<String> iconName,
                final Optional<String> iconStyle) {
            final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entityType).getKey();

            final IEntityCentreContextSelectorDone<AbstractEntity<?>> contextConfig = context().withCurrentEntity().withSelectionCrit().withComputation(computation.orElse((entity, context) -> entityType));

            return action(EntityNavigationAction.class)
                    .withContext(contextConfig.build())
                    .preAction(new SequentialEditPreAction())
                    .icon(iconName.orElse("editor:mode-edit"))
                    .withStyle(iconStyle.orElse(STANDARD_ACTION_COLOUR))
                    .shortDesc(format("Edit %s", entityTitle))
                    .longDesc(format("Edit %s", entityTitle))
                    .prefDimForView(prefDim.orElse(null))
                    .withNoParentCentreRefresh()
                    .build();
        }
    },

    DELETE_ACTION {

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType) {
            return mkAction(entityType, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation) {
            return mkAction(entityType, Optional.of(computation), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkActionWithIcon(final Class<? extends AbstractEntity<?>> entityType, final String iconName, final Optional<String> iconStyle) {
            return mkAction(entityType, Optional.empty(), Optional.empty(), Optional.of(iconName), iconStyle, Optional.empty());
        }

        @Override
        public EntityActionConfig mkActionWithPreAction(final Class<? extends AbstractEntity<?>> entityType, final IPreAction preAction) {
            return mkAction(entityType, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(preAction));
        }

        private EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType,
                final Optional<BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object>> computation,
                final Optional<PrefDim> prefDim,
                final Optional<String> iconName,
                final Optional<String> iconStyle,
                final Optional<IPreAction> preAction) {
            final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entityType).getKey();
            final String desc = format("Delete selected %s entities", entityTitle);

            final IEntityCentreContextSelectorDone<AbstractEntity<?>> contextConfig = context().withSelectedEntities().withComputation(computation.orElse((entity, context) -> entityType));

            return action(EntityDeleteAction.class)
                    .withContext(contextConfig.build())
                    .preAction(preAction.orElse(okCancel(DELETE_CONFIRMATION.msg)))
                    .postActionSuccess(() -> new JsCode("self.$.egi.clearPageSelection(); \n"))
                    .postActionError(() -> new JsCode("self.currentPage();\n"))
                    .icon(iconName.orElse("icons:remove-circle-outline"))
                    .withStyle(iconStyle.orElse(STANDARD_ACTION_COLOUR))
                    .shortDesc(desc)
                    .longDesc(desc)
                    .shortcut("alt+d")
                    .build();
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final PrefDim prefDim) {
            throw new UnsupportedOperationException("It's imposible to set preferred dimension for noUI maser!");
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation, final PrefDim prefDim) {
            throw new UnsupportedOperationException("It's imposible to set preferred dimension for noUI maser!");
        }

    },

    EXPORT_ACTION {
        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType) {
            return mkAction(entityType, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final PrefDim prefDim) {
            return mkAction(entityType, Optional.empty(), Optional.of(prefDim), Optional.empty(), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation) {
            return mkAction(entityType, Optional.of(computation), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation, final PrefDim prefDim) {
            return mkAction(entityType, Optional.of(computation), Optional.of(prefDim), Optional.empty(), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkActionWithIcon(final Class<? extends AbstractEntity<?>> entityType, final String iconName, final Optional<String> iconStyle) {
            return mkAction(entityType, Optional.empty(), Optional.empty(), Optional.of(iconName), iconStyle, Optional.empty());
        }

        @Override
        public EntityActionConfig mkActionWithPreAction(final Class<? extends AbstractEntity<?>> entityType, final IPreAction preAction) {
            return mkAction(entityType, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(preAction));
        }

        private EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType,
                final Optional<BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object>> computation,
                final Optional<PrefDim> prefDim,
                final Optional<String> iconName,
                final Optional<String> iconStyle,
                final Optional<IPreAction> preAction) {
            final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entityType).getKey();
            final String desc = format("Export selected %s entities", entityTitle);

            final IEntityCentreContextSelectorDone<AbstractEntity<?>> contextConfig = context().withSelectionCrit().withSelectedEntities().withComputation(computation.orElse((entity, context) -> entityType));

            return action(EntityExportAction.class)
                    .withContext(contextConfig.build())
                    .postActionSuccess(new FileSaverPostAction())
                    .icon(iconName.orElse("icons:save"))
                    .withStyle(iconStyle.orElse(STANDARD_ACTION_COLOUR))
                    .shortDesc(desc)
                    .longDesc(desc)
                    .prefDimForView(prefDim.orElse(null))
                    .withNoParentCentreRefresh()
                    .build();
        }
    },

    /* Export action requires a master entity in case of embedded centres in order to be able to execute the query correctly. */
    EXPORT_EMBEDDED_CENTRE_ACTION {
        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType) {
            return mkAction(entityType, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final PrefDim prefDim) {
            return mkAction(entityType, Optional.empty(), Optional.of(prefDim), Optional.empty(), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation) {
            return mkAction(entityType, Optional.of(computation), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation, final PrefDim prefDim) {
            return mkAction(entityType, Optional.of(computation), Optional.of(prefDim), Optional.empty(), Optional.empty(), Optional.empty());
        }

        @Override
        public EntityActionConfig mkActionWithIcon(final Class<? extends AbstractEntity<?>> entityType, final String iconName, final Optional<String> iconStyle) {
            return mkAction(entityType, Optional.empty(), Optional.empty(), Optional.of(iconName), iconStyle, Optional.empty());
        }

        @Override
        public EntityActionConfig mkActionWithPreAction(final Class<? extends AbstractEntity<?>> entityType, final IPreAction preAction) {
            return mkAction(entityType, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(preAction));
        }

        private EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType,
                final Optional<BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object>> computation,
                final Optional<PrefDim> prefDim,
                final Optional<String> iconName,
                final Optional<String> iconStyle,
                final Optional<IPreAction> preAction) {
            final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entityType).getKey();
            final String desc = format("Export selected %s entities", entityTitle);

            final IEntityCentreContextSelectorDone<AbstractEntity<?>> contextConfig = context().withSelectionCrit().withSelectedEntities().withMasterEntity().withComputation(computation.orElse((entity, context) -> entityType));

            return action(EntityExportAction.class)
                    .withContext(contextConfig.build())
                    .postActionSuccess(new FileSaverPostAction())
                    .icon(iconName.orElse("icons:save"))
                    .withStyle(iconStyle.orElse(STANDARD_ACTION_COLOUR))
                    .shortDesc(desc)
                    .longDesc(desc)
                    .prefDimForView(prefDim.orElse(null))
                    .withNoParentCentreRefresh()
                    .build();
        }
    };

    public abstract EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType);
    public abstract EntityActionConfig mkActionWithIcon(final Class<? extends AbstractEntity<?>> entityType, final String iconName, final Optional<String> iconStyle);
    public abstract EntityActionConfig mkActionWithPreAction(final Class<? extends AbstractEntity<?>> entityType, final IPreAction preAction);
    public abstract EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, PrefDim prefDim);
    public abstract EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation);
    public abstract EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation, PrefDim prefDim);

    /**
     * Makes property action configuration to be associated with properties of type {@code propType} on Entity Masters to facilitate ad hoc creation of values of that type with simultaneous assignment of the created values to the associated property. 
     * 
     * @param propName
     * @return
     */
    public static EntityActionConfig mkAddNewAndAssignPropAction(final String propName, final Class<? extends AbstractEntity<?>> propType) {
        final String actionDesc = actionAddDesc(getEntityTitleAndDesc(propType).getKey());
        return action(EntityNewAction.class)
                .withContext(context().withSelectionCrit().withComputation((entity, ctx) -> propType).build())
                .postActionSuccess(() -> new JsCode(format(""
                        + "if (functionalEntity.type().fullClassName() === '%s') {\n"
                        + "    self.setEditorValue4PropertyFromConcreteValue('%s', functionalEntity);\n"
                        + "}\n", propType.getName(), propName)))
                .icon("icons:add-circle-outline")
                .withStyle(STANDARD_ACTION_COLOUR)
                .shortDesc(actionDesc)
                .longDesc(actionDesc)
                .withNoParentCentreRefresh()
                .build();
    }

    /**
     * Produces a standard description for action Edit.
     *
     * @param entityTitle
     * @return
     */
    public static String actionEditDesc(final String entityTitle) {
        return "Edit " + entityTitle;
    }

    /**
     * Produces a standard description for action Add.
     *
     * @param entityTitle
     * @return
     */
    public static String actionAddDesc(final String entityTitle) {
        return "Add new " + entityTitle;
    }

    /**
     * Produces a standard description when reviewing data via secondary action.
     *
     * @param entityTitle
     * @return
     */
    public static String actionReviewDesc(final String entityTitle) {
        return "Review " + entityTitle;
    }

}
