package ua.com.fielden.platform.web.action;

import static java.util.Optional.empty;
import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.action.StandardMastersWebUiConfig.MASTER_ACTION_DEFAULT_WIDTH;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.MOBILE;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.TABLET;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.cell;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.html;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutCellBuilder.layout;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.mkActionLayoutForMaster;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.mkGridForMasterFitWidth;
import static ua.com.fielden.platform.web.view.master.EntityMaster.noUiFunctionalMaster;
import static ua.com.fielden.platform.web.view.master.api.actions.MasterActions.REFRESH;
import static ua.com.fielden.platform.web.view.master.api.actions.MasterActions.SAVE;

import com.google.inject.Injector;

import ua.com.fielden.platform.web.centre.AbstractCentreConfigCommitAction;
import ua.com.fielden.platform.web.centre.CentreColumnWidthConfigUpdater;
import ua.com.fielden.platform.web.centre.CentreColumnWidthConfigUpdaterProducer;
import ua.com.fielden.platform.web.centre.CentreConfigConfigureAction;
import ua.com.fielden.platform.web.centre.CentreConfigConfigureActionProducer;
import ua.com.fielden.platform.web.centre.CentreConfigDeleteAction;
import ua.com.fielden.platform.web.centre.CentreConfigDeleteActionProducer;
import ua.com.fielden.platform.web.centre.CentreConfigDuplicateAction;
import ua.com.fielden.platform.web.centre.CentreConfigDuplicateActionProducer;
import ua.com.fielden.platform.web.centre.CentreConfigEditAction;
import ua.com.fielden.platform.web.centre.CentreConfigEditActionProducer;
import ua.com.fielden.platform.web.centre.CentreConfigLoadAction;
import ua.com.fielden.platform.web.centre.CentreConfigLoadActionProducer;
import ua.com.fielden.platform.web.centre.CentreConfigNewAction;
import ua.com.fielden.platform.web.centre.CentreConfigNewActionProducer;
import ua.com.fielden.platform.web.centre.CentreConfigSaveAction;
import ua.com.fielden.platform.web.centre.CentreConfigSaveActionProducer;
import ua.com.fielden.platform.web.centre.CentreConfigShareAction;
import ua.com.fielden.platform.web.centre.CentreConfigUpdater;
import ua.com.fielden.platform.web.centre.CentreConfigUpdaterProducer;
import ua.com.fielden.platform.web.centre.CentrePreferredViewUpdater;
import ua.com.fielden.platform.web.centre.CentrePreferredViewUpdaterProducer;
import ua.com.fielden.platform.web.centre.OverrideCentreConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.layout.api.impl.FlexLayoutConfig;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

/**
 * Web UI configuration for centre actions.
 *
 * @author TG Team
 *
 */
public class CentreConfigurationWebUiConfig {
    public final EntityMaster<CentreConfigUpdater> centreConfigUpdaterMaster;
    public final EntityMaster<CentreColumnWidthConfigUpdater> centreColumnWidthConfigUpdaterMaster;
    public final EntityMaster<CentrePreferredViewUpdater> centrePreferredViewUpdaterMaster;
    public final EntityMaster<CentreConfigShareAction> centreConfigShareActionMaster;
    public final EntityMaster<CentreConfigNewAction> centreConfigNewActionMaster;
    public final EntityMaster<CentreConfigDuplicateAction> centreConfigDuplicateActionMaster;
    public final EntityMaster<CentreConfigLoadAction> centreConfigLoadActionMaster;
    public final EntityMaster<CentreConfigEditAction> centreConfigEditActionMaster;
    public final EntityMaster<CentreConfigDeleteAction> centreConfigDeleteActionMaster;
    public final EntityMaster<CentreConfigSaveAction> centreConfigSaveActionMaster;
    public final EntityMaster<OverrideCentreConfig> overrideCentreConfigMaster;
    public final EntityMaster<CentreConfigConfigureAction> centreConfigConfigureActionMaster;

    public CentreConfigurationWebUiConfig(final Injector injector) {
        centreConfigUpdaterMaster = createCentreConfigUpdater(injector,
                "['padding:20px', 'height: 100%', 'box-sizing: border-box', ['flex', ['flex']], [['flex', 'padding-right:20px'], ['flex', 'padding-right:20px'], ['flex']]]",
                "['padding:20px', 'height: 100%', 'box-sizing: border-box', ['flex', ['flex']], [], [], []]");
        centreColumnWidthConfigUpdaterMaster = noUiFunctionalMaster(CentreColumnWidthConfigUpdater.class, CentreColumnWidthConfigUpdaterProducer.class, injector);
        centrePreferredViewUpdaterMaster = noUiFunctionalMaster(CentrePreferredViewUpdater.class, CentrePreferredViewUpdaterProducer.class, injector);
        centreConfigShareActionMaster = noUiFunctionalMaster(CentreConfigShareAction.class, injector);
        centreConfigNewActionMaster = noUiFunctionalMaster(CentreConfigNewAction.class, CentreConfigNewActionProducer.class, injector);
        centreConfigDuplicateActionMaster = noUiFunctionalMaster(CentreConfigDuplicateAction.class, CentreConfigDuplicateActionProducer.class, injector);
        centreConfigLoadActionMaster = createCentreConfigLoadActionMaster(injector, "['padding:20px', 'height: 100%', 'box-sizing: border-box', ['flex', ['flex']]]");
        centreConfigEditActionMaster = createCentreConfigEditActionMaster(injector);
        centreConfigDeleteActionMaster = noUiFunctionalMaster(CentreConfigDeleteAction.class, CentreConfigDeleteActionProducer.class, injector);
        centreConfigSaveActionMaster = createCentreConfigSaveActionMaster(injector);
        overrideCentreConfigMaster = createOverrideCentreConfigMaster(injector);
        centreConfigConfigureActionMaster = createCentreConfigConfigureActionMaster(injector);
    }

    /**
     * Creates entity master for {@link CentreConfigUpdater}.
     * @param masterMobileLayout
     *
     * @return
     */
    private static EntityMaster<CentreConfigUpdater> createCentreConfigUpdater(final Injector injector, final String masterLayout, final String masterMobileLayout) {
        final FlexLayoutConfig horizontal = layout().withClass("wrap").withStyle("padding", "10px").horizontal().centerJustified().end();
        final String actionLayout = cell(cell().cell().layoutForEach(layout().withStyle("width", MASTER_ACTION_DEFAULT_WIDTH + "px").withStyle("margin", "0px 10px 10px 10px").end()), horizontal).toString();
        final IMaster<CentreConfigUpdater> masterConfig = new SimpleMasterBuilder<CentreConfigUpdater>()
                .forEntity(CentreConfigUpdater.class)
                .addProp("customisableColumns").asCollectionalEditor().reorderable().withHeader("title").also()
                .addProp("pageCapacity").asSpinner().also()
                .addProp("visibleRowsCount").asSpinner().also()
                .addProp("numberOfHeaderLines").asSpinner().also()
                .addAction(REFRESH).shortDesc("CANCEL").longDesc("Cancel not applied changes and close the dialog.")
                .addAction(SAVE).shortDesc("APPLY").longDesc("Apply changes.").keepMasterOpenAfterExecution()
                .setActionBarLayoutFor(DESKTOP, empty(), actionLayout)
                .setActionBarLayoutFor(TABLET, empty(), actionLayout)
                .setActionBarLayoutFor(MOBILE, empty(), actionLayout)
                .setLayoutFor(DESKTOP, empty(), masterLayout)
                .setLayoutFor(TABLET, empty(), masterLayout)
                .setLayoutFor(MOBILE, empty(), masterMobileLayout)
                .withDimensions(mkDim(576, 468))
                .done();
        return new EntityMaster<>(CentreConfigUpdater.class, CentreConfigUpdaterProducer.class, masterConfig, injector);
    }

    public enum CentreConfigActions {
        CUSTOMISE_COLUMNS_ACTION {
            @Override
            public EntityActionConfig mkAction() {
                return action(CentreConfigUpdater.class)
                        .withContext(context().withSelectionCrit().build())
                        .postActionSuccess(() ->// self.run should be invoked with isSortingAction=true parameter (and isAutoRunning=undefined). See tg-entity-centre-behavior 'run' property for more details.
                                new JsCode(""
                                    // if pageCapacity has been changed then self.$.selection_criteria.pageCapacity will be updated after re-running in tg-entity-centre-behavior._postRun; otherwise -- no need to update it
                                    + "    self.$.egi.visibleRowsCount = functionalEntity.get('visibleRowsCount');\n"
                                    + "    self.$.egi.numberOfHeaderLines = functionalEntity.get('numberOfHeaderLines');\n"
                                    + "    if (functionalEntity.get('triggerRerun') === true) {\n"
                                    + "        return self.retrieve().then(function () { self.run(undefined, true); });\n"
                                    + "    } else {\n"
                                    + "        self.$.egi.adjustColumnsVisibility(functionalEntity.get('chosenIds').map(column => column === 'this' ? '' : column));\n"
                                    + "        self.$.selection_criteria._centreDirty = functionalEntity.get('centreDirty');\n"
                                    + "    }\n"
                                    + ""))
                        .icon("av:sort-by-alpha")
                        .shortDesc("Customise Columns")
                        .longDesc("Customise columns for this centre.")
                        .withNoParentCentreRefresh()
                        .build();
            }
        };

        public abstract EntityActionConfig mkAction();
    }

    /**
     * Creates entity master for {@link CentreConfigEditAction}.
     *
     * @return
     */
    private static EntityMaster<CentreConfigEditAction> createCentreConfigEditActionMaster(final Injector injector) {
        return new EntityMaster<>(
            CentreConfigEditAction.class,
            CentreConfigEditActionProducer.class,
            createCentreConfigCommitActionMaster(injector, CentreConfigEditAction.class, "Save title and description changes.", "Cancel changes."),
            injector
        );
    }

    /**
     * Creates entity master for {@link CentreConfigConfigureAction}.
     *
     * @return
     */
    private static EntityMaster<CentreConfigConfigureAction> createCentreConfigConfigureActionMaster(final Injector injector) {
        return new EntityMaster<>(
            CentreConfigConfigureAction.class,
            CentreConfigConfigureActionProducer.class,
            createCentreConfigConfigureActionMasterConfiguration(injector),
            injector
        );
    }

    /**
     * Creates {@link IMaster} configuration for {@link CentreConfigConfigureAction} entity.
     *
     * @param injector
     * @return
     */
    private static IMaster<CentreConfigConfigureAction> createCentreConfigConfigureActionMasterConfiguration(final Injector injector) {
        final String actionLayout = mkActionLayoutForMaster();
        final String layout = mkGridForMasterFitWidth(1, 1);

        return new SimpleMasterBuilder<CentreConfigConfigureAction>()
            .forEntity(CentreConfigConfigureAction.class)
            .addProp("runAutomatically").asCheckbox().also()
            .addAction(REFRESH).shortDesc("CANCEL").longDesc("Cancel changes.")
            .addAction(SAVE).shortDesc("SAVE").longDesc("Save changes.")
            .setActionBarLayoutFor(DESKTOP, empty(), actionLayout)
            .setActionBarLayoutFor(TABLET, empty(), actionLayout)
            .setActionBarLayoutFor(MOBILE, empty(), actionLayout)
            .setLayoutFor(DESKTOP, empty(), layout)
            .setLayoutFor(TABLET, empty(), layout)
            .setLayoutFor(MOBILE, empty(), layout)
            .withDimensions(mkDim(238, 177))
            .done();
    }

    /**
     * Creates {@link IMaster} configuration for {@link AbstractCentreConfigCommitAction} descendant entities.
     *
     * @param injector
     * @param entityType
     * @param customSaveDesc -- custom tooltip of SAVE button
     * @param customCancelDesc -- custom tooltip of CANCEL button
     * @return
     */
    private static <T extends AbstractCentreConfigCommitAction> IMaster<T> createCentreConfigCommitActionMaster(final Injector injector, final Class<T> entityType, final String customSaveDesc, final String customCancelDesc) {
        final String actionLayout = mkActionLayoutForMaster();
        final String layout = mkGridForMasterFitWidth(4, 1);

        return new SimpleMasterBuilder<T>()
            .forEntity(entityType)
            .addProp("title").asSinglelineText().also()
            .addProp("desc").asMultilineText().also()
            .addProp("dashboardable").asCheckbox().also()
            .addProp("dashboardRefreshFrequency").asAutocompleter().also()
            .addAction(REFRESH).shortDesc("CANCEL").longDesc(customCancelDesc)
            .addAction(SAVE).shortDesc("SAVE").longDesc(customSaveDesc)
            .setActionBarLayoutFor(DESKTOP, empty(), actionLayout)
            .setActionBarLayoutFor(TABLET, empty(), actionLayout)
            .setActionBarLayoutFor(MOBILE, empty(), actionLayout)
            .setLayoutFor(DESKTOP, empty(), layout)
            .setLayoutFor(TABLET, empty(), layout)
            .setLayoutFor(MOBILE, empty(), layout)
            .withDimensions(mkDim(400, 383))
            .done();
    }
    /**
     * Creates entity master for {@link CentreConfigSaveAction}.
     *
     * @return
     */
    private static EntityMaster<CentreConfigSaveAction> createCentreConfigSaveActionMaster(final Injector injector) {
        return new EntityMaster<>(
            CentreConfigSaveAction.class,
            CentreConfigSaveActionProducer.class,
            createCentreConfigCommitActionMaster(injector, CentreConfigSaveAction.class, "Save this new configuration.", "Cancel saving this new configuration."),
            injector
        );
    }

    /**
     * Creates entity master for {@link CentreConfigLoadAction}.
     *
     * @return
     */
    private static EntityMaster<CentreConfigLoadAction> createCentreConfigLoadActionMaster(final Injector injector, final String masterLayout) {
        final String actionLayout = mkActionLayoutForMaster();
        final IMaster<CentreConfigLoadAction> masterConfig = new SimpleMasterBuilder<CentreConfigLoadAction>()
            .forEntity(CentreConfigLoadAction.class)
            .addProp("centreConfigurations").asCollectionalEditor().also()
            .addAction(REFRESH).shortDesc("CANCEL").longDesc("Cancels configuration loading.")
            .addAction(SAVE).shortDesc("LOAD").longDesc("Loads currently chosen configuration.")
            .setActionBarLayoutFor(DESKTOP, empty(), actionLayout)
            .setActionBarLayoutFor(TABLET, empty(), actionLayout)
            .setActionBarLayoutFor(MOBILE, empty(), actionLayout)
            .setLayoutFor(DESKTOP, empty(), masterLayout)
            .setLayoutFor(TABLET, empty(), masterLayout)
            .setLayoutFor(MOBILE, empty(), masterLayout)
            .withDimensions(mkDim("'30%'", "'50%'"))
            .done();
        return new EntityMaster<>(CentreConfigLoadAction.class, CentreConfigLoadActionProducer.class, masterConfig, injector);
    }

    /**
     * Creates entity master for {@link OverrideCentreConfig}.
     *
     * @return
     */
    private EntityMaster<OverrideCentreConfig> createOverrideCentreConfigMaster(final Injector injector) {
        final String layout = cell(
            html("<div style='text-align: center; color: #232F34; font-size: 14px; font-weight: 300;'>Configuration with this title already exists. Override?<div>"),
            layout().withStyle("padding", "24px 24px 28px 24px").end()
        ).toString();
        final String actionLayout = mkActionLayoutForMaster();

        final IMaster<OverrideCentreConfig> masterConfig = new SimpleMasterBuilder<OverrideCentreConfig>().forEntity(OverrideCentreConfig.class)
                .addAction(REFRESH).shortDesc("NO")
                .addAction(SAVE).shortDesc("YES")
                .setActionBarLayoutFor(DESKTOP, empty(), actionLayout)
                .setActionBarLayoutFor(TABLET, empty(), actionLayout)
                .setActionBarLayoutFor(MOBILE, empty(), actionLayout)
                .setLayoutFor(DESKTOP, empty(), layout)
                .setLayoutFor(TABLET, empty(), layout)
                .setLayoutFor(MOBILE, empty(), layout)
                // initial dimensions are defined by 'Configuration with this title already exists. Override?' message length
                .done();

        return new EntityMaster<>(OverrideCentreConfig.class, masterConfig, injector);
    }

}