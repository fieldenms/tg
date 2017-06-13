package ua.com.fielden.platform.web.test.server.config;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.action.pre.ConfirmationPreAction.okCancel;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.test.server.config.StandardMessages.DELETE_CONFIRMATION;

import java.util.function.Function;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.EntityDeleteAction;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.entity.EntityExportAction;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.web.PrefDim;
import ua.com.fielden.platform.web.action.post.FileSaverPostAction;
import ua.com.fielden.platform.web.action.pre.ExportPreAction;
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
            return mkAction(entityType, null, null);
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final PrefDim prefDim) {
            return mkAction(entityType, null, prefDim);
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final Function<AbstractFunctionalEntityWithCentreContext<?>, Object> computation) {
            return mkAction(entityType, computation, null);
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final Function<AbstractFunctionalEntityWithCentreContext<?>, Object> computation, final PrefDim prefDim) {
            final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entityType).getKey();

            final IEntityCentreContextSelectorDone<AbstractEntity<?>> contextConfig;
            if (computation != null) {
                contextConfig = context().withSelectionCrit().withComputation(computation);
            } else {
                contextConfig = context().withSelectionCrit().withComputation(entity -> entityType);
            }

            return action(EntityNewAction.class).
                    withContext(contextConfig.build()).
                    icon("add-circle-outline").
                    shortDesc(format("Add new %s", entityTitle)).
                    longDesc(format("Start creation of %s", entityTitle)).
                    shortcut("alt+n").
                    prefDimForView(prefDim).
                    withNoParentCentreRefresh().
                    build();
        }
    },

    NEW_WITH_MASTER_ACTION {
        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType) {
            return mkAction(entityType, null, null);
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final PrefDim prefDim) {
            return mkAction(entityType, null, prefDim);
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final Function<AbstractFunctionalEntityWithCentreContext<?>, Object> computation) {
            return mkAction(entityType, computation, null);
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final Function<AbstractFunctionalEntityWithCentreContext<?>, Object> computation, final PrefDim prefDim) {
            final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entityType).getKey();

            final IEntityCentreContextSelectorDone<AbstractEntity<?>> contextConfig;
            if (computation != null) {
                contextConfig = context().withMasterEntity().withSelectionCrit().withComputation(computation);
            } else {
                contextConfig = context().withMasterEntity().withSelectionCrit().withComputation(entity -> entityType);
            }

            return action(EntityNewAction.class).
                    withContext(contextConfig.build()).
                    icon("add-circle-outline").
                    shortDesc(format("Add new %s", entityTitle)).
                    longDesc(format("Start creation of %s", entityTitle)).
                    shortcut("alt+n").
                    prefDimForView(prefDim).
                    withNoParentCentreRefresh().
                    build();
        }
    },


    EDIT_ACTION {
        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType) {
            return mkAction(entityType, null, null);
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final PrefDim prefDim) {
            return mkAction(entityType, null, prefDim);
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final Function<AbstractFunctionalEntityWithCentreContext<?>, Object> computation) {
            return mkAction(entityType, computation, null);
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final Function<AbstractFunctionalEntityWithCentreContext<?>, Object> computation, final PrefDim prefDim) {
            final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entityType).getKey();

            final IEntityCentreContextSelectorDone<AbstractEntity<?>> contextConfig;
            if (computation != null) {
                contextConfig = context().withCurrentEntity().withSelectionCrit().withComputation(computation);
            } else {
                contextConfig = context().withCurrentEntity().withSelectionCrit().withComputation(entity -> entityType);
            }

            return action(EntityEditAction.class).
                    withContext(contextConfig.build()).
                    icon("editor:mode-edit").
                    shortDesc(format("Edit %s", entityTitle)).
                    longDesc(format("Opens master for editing %s", entityTitle)).
                    prefDimForView(prefDim).
                    withNoParentCentreRefresh().
                    build();
        }
    },

    SEQUENTIAL_EDIT_ACTION {

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType) {
            return mkAction(entityType, null, null);
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final PrefDim prefDim) {
            return mkAction(entityType, null, prefDim);
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final Function<AbstractFunctionalEntityWithCentreContext<?>, Object> computation) {
            return mkAction(entityType, computation, null);
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final Function<AbstractFunctionalEntityWithCentreContext<?>, Object> computation, final PrefDim prefDim) {
            final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entityType).getKey();

            final IEntityCentreContextSelectorDone<AbstractEntity<?>> contextConfig;
            if (computation != null) {
                contextConfig = context().withCurrentEntity().withSelectionCrit().withComputation(computation);
            } else {
                contextConfig = context().withCurrentEntity().withSelectionCrit().withComputation(entity -> entityType);
            }

            return action(EntityEditAction.class).withContext(contextConfig.build()).preAction(new IPreAction() {

                @Override
                public JsCode build() {
                    return new JsCode("\n"
                            + "if(!self.seqEditIds) {\n"
                            + "    const selectedEntitiesToEdit = self.$.egi.getSelectedEntities();\n"
                            + "    self.seqEditIds = selectedEntitiesToEdit.length > 0 ? selectedEntitiesToEdit : self.$.egi.entities.slice();\n"
                            + "    if (self.seqEditIds.length > 0) {\n"
                            + "        action.currentEntity = self.seqEditIds.shift();\n"
                            + "    }\n"
                            + "    const cancelEditing = (function (data) {\n"
                            + "        delete this.seqEditIds;\n"
                            + "        this.seqEditSuccessPostal.unsubscribe();\n"
                            + "        this.seqEditCancelPostal.unsubscribe();\n"
                            + "    }).bind(self);\n"
                            + "    const updateCacheAndContinueSeqSaving = (function (data) {\n"
                            + "        const nextEntity = this.seqEditIds.shift();\n"
                            + "        if (nextEntity) {\n"
                            + "            action.currentEntity = nextEntity;\n"
                            + "            action._run();\n"
                            + "        } else {\n"
                            + "            cancelEditing(data);\n"
                            + "        }\n"
                            + "    }).bind(self);\n"
                            + "    action.continuous = true;\n"
                            + "    action.skipNext = function() {\n"
                            + "        updateCacheAndContinueSeqSaving();\n"
                            + "    };\n"
                            + "    self.seqEditSuccessPostal = postal.subscribe({\n"
                            + "        channel: self.uuid,\n"
                            + "        topic: 'save.post.success',\n"
                            + "        callback: updateCacheAndContinueSeqSaving\n"
                            + "    });\n"
                            + "    self.seqEditCancelPostal = postal.subscribe({\n"
                            + "        channel: self.uuid,\n"
                            + "        topic: 'refresh.post.success',\n"
                            + "        callback: cancelEditing"
                            + "    });\n"
                            + "}\n");
                }
            }).
            icon("editor:mode-edit").
            shortDesc(format("Edit %s", entityTitle)).
            longDesc(format("Edit %s", entityTitle)).
            prefDimForView(prefDim).
            withNoParentCentreRefresh().
            build();
        }
    },

    DELETE_ACTION {
        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType) {
            return mkAction(entityType, (Function<AbstractFunctionalEntityWithCentreContext<?>, Object>) null);
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final Function<AbstractFunctionalEntityWithCentreContext<?>, Object> computation) {
            final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entityType).getKey();
            final String desc = format("Delete selected %s entities", entityTitle);

            final IEntityCentreContextSelectorDone<AbstractEntity<?>> contextConfig;
            if (computation != null) {
                contextConfig = context().withSelectedEntities().withComputation(computation);
            } else {
                contextConfig = context().withSelectedEntities().withComputation(entity -> entityType);
            }

            return action(EntityDeleteAction.class).withContext(contextConfig.build()).preAction(okCancel(DELETE_CONFIRMATION.msg)).icon("remove-circle-outline").shortDesc(desc).longDesc(desc).shortcut("alt+d").build();
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final PrefDim prefDim) {
            throw new UnsupportedOperationException("It's imposible to set preferred dimension for noUI maser!");
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final Function<AbstractFunctionalEntityWithCentreContext<?>, Object> computation, final PrefDim prefDim) {
            throw new UnsupportedOperationException("It's imposible to set preferred dimension for noUI maser!");
        }

    },

    EXPORT_ACTION {
        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType) {
            return mkAction(entityType, null, null);
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final PrefDim prefDim) {
            return mkAction(entityType, null, prefDim);
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final Function<AbstractFunctionalEntityWithCentreContext<?>, Object> computation) {
            return mkAction(entityType, computation, null);
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final Function<AbstractFunctionalEntityWithCentreContext<?>, Object> computation, final PrefDim prefDim) {
            final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entityType).getKey();
            final String desc = format("Export selected %s entities", entityTitle);

            final IEntityCentreContextSelectorDone<AbstractEntity<?>> contextConfig;
            if (computation != null) {
                contextConfig = context().withSelectionCrit().withSelectedEntities().withComputation(computation);
            } else {
                contextConfig = context().withSelectionCrit().withSelectedEntities().withComputation(entity -> entityType);
            }

            return action(EntityExportAction.class).
                    withContext(contextConfig.build()).
                    preAction(new ExportPreAction()).
                    postActionSuccess(new FileSaverPostAction()).
                    icon("icons:save").
                    shortDesc(desc).
                    longDesc(desc).
                    prefDimForView(prefDim).
                    withNoParentCentreRefresh().
                    build();
        }
    },

    EXPORT_EMBEDDED_CENTRE_ACTION {
        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType) {
            return mkAction(entityType, null, null);
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final PrefDim prefDim) {
            return mkAction(entityType, null, prefDim);
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final Function<AbstractFunctionalEntityWithCentreContext<?>, Object> computation) {
            return mkAction(entityType, computation, null);
        }

        @Override
        public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final Function<AbstractFunctionalEntityWithCentreContext<?>, Object> computation, final PrefDim prefDim) {
            final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entityType).getKey();
            final String desc = format("Export selected %s entities", entityTitle);

            final IEntityCentreContextSelectorDone<AbstractEntity<?>> contextConfig;
            if (computation != null) {
                contextConfig = context().withSelectionCrit().withSelectedEntities().withComputation(computation);
            } else {
                contextConfig = context().withSelectionCrit().withSelectedEntities().withComputation(entity -> entityType);
            }

            return action(EntityExportAction.class).
                    withContext(contextConfig.build()).
                    preAction(new ExportPreAction()).
                    postActionSuccess(new FileSaverPostAction()).
                    icon("icons:save").
                    shortDesc(desc).
                    longDesc(desc).
                    prefDimForView(prefDim).
                    withNoParentCentreRefresh().
                    build();
        }
    };

    public abstract EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType);
    public abstract EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, PrefDim prefDim);
    public abstract EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final Function<AbstractFunctionalEntityWithCentreContext<?>, Object> computation);
    public abstract EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final Function<AbstractFunctionalEntityWithCentreContext<?>, Object> computation, PrefDim prefDim);
}
