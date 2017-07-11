package ua.com.fielden.platform.web.action;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.web.centre.CentreColumnWidthConfigUpdater;
import ua.com.fielden.platform.web.centre.CentreConfigUpdater;
import ua.com.fielden.platform.web.centre.CentreConfigUpdaterDefaultAction;
import ua.com.fielden.platform.web.centre.CentreConfigUpdaterDefaultActionProducer;
import ua.com.fielden.platform.web.centre.CentreConfigUpdaterProducer;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

/**
 * Web UI configuration for {@link CentreConfigUpdater}.
 *
 * @author TG Team
 *
 */
public class CentreConfigurationWebUiConfig {
    private static final String actionButton = "'margin: 10px', 'width: 110px'";
    private static final String bottomButtonPanel = "['horizontal', 'padding: 20px', 'justify-content: center', 'wrap', [%s], [%s], [%s]]";

    public final EntityMaster<CentreConfigUpdater> centreConfigUpdater;
    public final EntityMaster<CentreConfigUpdaterDefaultAction> centreConfigUpdaterDefaultAction;
    public final EntityMaster<CentreColumnWidthConfigUpdater> centreColumnWidthConfigUpdater;

    public CentreConfigurationWebUiConfig(final Injector injector) {
        centreConfigUpdater = createCentreConfigUpdater(injector);
        centreConfigUpdaterDefaultAction = createCentreConfigUpdaterDefaultAction(injector);
        centreColumnWidthConfigUpdater = createCentreColumnWidthConfigUpdater(injector);
    }

    /**
     * Creates entity master for {@link CentreConfigUpdater}.
     *
     * @return
     */
    private static EntityMaster<CentreConfigUpdater> createCentreConfigUpdater(final Injector injector) {
        final IMaster<CentreConfigUpdater> masterConfig = new SimpleMasterBuilder<CentreConfigUpdater>()
                .forEntity(CentreConfigUpdater.class)
                .addProp("customisableColumns").asCollectionalEditor().maxVisibleRows(5).withHeader("title")
                .also()
                .addAction(
                        action(CentreConfigUpdaterDefaultAction.class)
                        .withContext(context().withMasterEntity().build())
                        .postActionSuccess(
                            new IPostAction() {
                                @Override
                                public JsCode build() {
                                    return new JsCode(""
                                        + "const editor = self.$.masterDom.querySelector('[id=editor_4_customisableColumns]');\n"
                                        + "editor._originalChosenIds = null; // this should trigger full refresh \n"
                                        + "editor.entity.setAndRegisterPropertyTouch('chosenIds', functionalEntity.get('defaultVisibleProperties'));\n"
                                        + "editor.entity.sortingVals = functionalEntity.get('defaultSortingVals');\n"
                                        + "editor._invokeValidation.bind(editor)();\n"
                                    );
                                }
                            })
                        .shortDesc("Default")
                        .longDesc("Load default configuration")
                        .shortcut("ctrl+d")
                        .build())
                .addAction(MasterActions.REFRESH).shortDesc("CANCEL").longDesc("Cancel action")
                .addAction(MasterActions.SAVE).shortDesc("CUSTOMISE").longDesc("Customise columns action")

                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), format(bottomButtonPanel, actionButton, actionButton, actionButton))
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
    
    /**
     * Creates no-ui entity master for {@link CentreConfigUpdaterDefaultAction}.
     *
     * @return
     */
    private static EntityMaster<CentreConfigUpdaterDefaultAction> createCentreConfigUpdaterDefaultAction(final Injector injector) {
        return new EntityMaster<CentreConfigUpdaterDefaultAction>(CentreConfigUpdaterDefaultAction.class, CentreConfigUpdaterDefaultActionProducer.class, null, injector);
    }

    /**
     * Creates no-ui entity master for {@link CentreColumWidthConfigUpdater}.
     *
     * @return
     */
    private static EntityMaster<CentreColumnWidthConfigUpdater> createCentreColumnWidthConfigUpdater(final Injector injector) {
        return new EntityMaster<CentreColumnWidthConfigUpdater>(CentreColumnWidthConfigUpdater.class, null, injector);
    }

    public enum CentreConfigActions {
        SORT_ACTION { // TODO change name to CUSTOMISE_COLUMNS_ACTION
            @Override
            public EntityActionConfig mkAction() {
                return action(CentreConfigUpdater.class)
                        .withContext(context().withSelectionCrit().build())
                        .postActionSuccess(new IPostAction() {
                            @Override
                            public JsCode build() {
                                // self.run should be invoked with isSortingAction=true parameter. See tg-entity-centre-behavior 'run' property for more details.
                                return new JsCode(""
                                    + "    if (functionalEntity.get('sortingChanged') === true) {\n"
                                    + "        return self.retrieve().then(function () { self.run(true); });\n"
                                    + "    } else {\n"
                                    + "        self.$.egi._adjustColumns(functionalEntity.get('chosenIds'));\n"
                                    + "    }\n"
                                    + "");
                            }
                        })
                        .icon("av:sort-by-alpha")
                        .shortDesc("Customise Columns")
                        .longDesc("Customise columns for this centre.")
                        .withNoParentCentreRefresh()
                        .build();
            }
        };

        public abstract EntityActionConfig mkAction();
    }
}
