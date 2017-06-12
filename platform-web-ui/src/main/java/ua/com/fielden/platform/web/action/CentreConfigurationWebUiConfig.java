package ua.com.fielden.platform.web.action;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;

import java.util.Optional;
import java.util.function.Function;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.web.centre.CentreConfigUpdater;
import ua.com.fielden.platform.web.centre.CentreConfigUpdaterProducer;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.context.IEntityCentreContextSelectorDone;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

/**
 * Web UI configuration for {@link CentreConfigUpdater}.
 *
 * @author TG Team
 *
 */
public class CentreConfigurationWebUiConfig {
    private static final String actionButton = "'margin: 10px', 'width: 110px'";
    private static final String bottomButtonPanel = "['horizontal', 'padding: 20px', 'justify-content: center', 'wrap', [%s], [%s]]";

    public final EntityMaster<CentreConfigUpdater> centreConfigUpdater;

    public CentreConfigurationWebUiConfig(final Injector injector) {
        centreConfigUpdater = createCentreConfigUpdater(injector);
    }

    /**
     * Creates entity master for {@link CentreConfigUpdater}.
     *
     * @return
     */
    private static EntityMaster<CentreConfigUpdater> createCentreConfigUpdater(final Injector injector) {
        final IMaster<CentreConfigUpdater> masterConfig = new SimpleMasterBuilder<CentreConfigUpdater>()
                .forEntity(CentreConfigUpdater.class)
                .addProp("sortingProperties").asCollectionalEditor().maxVisibleRows(5).withHeader("title")
                .also()
                .addAction(MasterActions.REFRESH).shortDesc("CANCEL").longDesc("Cancel action")
                .addAction(MasterActions.SAVE).shortDesc("SORT").longDesc("Sorting action")

                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), format(bottomButtonPanel, actionButton, actionButton))
                .setLayoutFor(Device.DESKTOP, Optional.empty(), (
                        "      ['padding:20px', 'width:500px', "
                        + format("['flex', ['flex']]")
                        + "    ]"))
                .done();
        return new EntityMaster<CentreConfigUpdater>(
                CentreConfigUpdater.class,
                CentreConfigUpdaterProducer.class,
                masterConfig,
                injector);
    }

    public enum CentreConfigActions {
        SORT_ACTION {
            @Override
            public EntityActionConfig mkAction() {
                return action(CentreConfigUpdater.class)
                        .withContext(context().withSelectionCrit().build())
                        .postActionSuccess(new IPostAction() {
                            @Override
                            public JsCode build() {
                                // self.run should be invoked with isSortingAction=true parameter. See tg-entity-centre-behavior 'run' property for more details.
                                return new JsCode("   return self.retrieve().then(function () { self.run(true); }); \n");
                            }
                        })
                        .icon("av:sort-by-alpha")
                        .shortDesc("Change Sorting")
                        .longDesc("Change sorting properties for this centre.")
                        .withNoParentCentreRefresh()
                        .build();
            }

            @Override
            public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final Function<AbstractFunctionalEntityWithCentreContext<?>, Object> computation) {
                return mkAction();
            }

            @Override
            public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType) {
                return mkAction();
            }

        },

        SEQUENTIAL_EDIT_ACTION {

            @Override
            public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final Function<AbstractFunctionalEntityWithCentreContext<?>, Object> computation) {
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
                })
                .icon("editor:mode-edit")
                .shortDesc(format("Edit %s", entityTitle))
                .longDesc(format("Edit %s", entityTitle))
                .withNoParentCentreRefresh()
                .build();
            }

            @Override
            public EntityActionConfig mkAction() {
                throw new UnsupportedOperationException("This type of make action is not supported please specify entity type!");
            }

            @Override
            public EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType) {
                return mkAction(entityType, null);
            }
        };

        public abstract EntityActionConfig mkAction();

        public abstract EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType);

        public abstract EntityActionConfig mkAction(final Class<? extends AbstractEntity<?>> entityType, final Function<AbstractFunctionalEntityWithCentreContext<?>, Object> computation);
    }
}
