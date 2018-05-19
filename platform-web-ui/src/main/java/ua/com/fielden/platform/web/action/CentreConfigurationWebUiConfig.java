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
import static ua.com.fielden.platform.web.view.master.api.actions.MasterActions.REFRESH;
import static ua.com.fielden.platform.web.view.master.api.actions.MasterActions.SAVE;

import com.google.inject.Injector;

import ua.com.fielden.platform.web.centre.CentreColumnWidthConfigUpdater;
import ua.com.fielden.platform.web.centre.CentreColumnWidthConfigUpdaterProducer;
import ua.com.fielden.platform.web.centre.CentreConfigDeleteAction;
import ua.com.fielden.platform.web.centre.CentreConfigDeleteActionProducer;
import ua.com.fielden.platform.web.centre.CentreConfigEditAction;
import ua.com.fielden.platform.web.centre.CentreConfigEditActionProducer;
import ua.com.fielden.platform.web.centre.CentreConfigLoadAction;
import ua.com.fielden.platform.web.centre.CentreConfigLoadActionProducer;
import ua.com.fielden.platform.web.centre.CentreConfigUpdater;
import ua.com.fielden.platform.web.centre.CentreConfigUpdaterDefaultAction;
import ua.com.fielden.platform.web.centre.CentreConfigUpdaterDefaultActionProducer;
import ua.com.fielden.platform.web.centre.CentreConfigUpdaterProducer;
import ua.com.fielden.platform.web.centre.OverrideCentreConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.layout.api.impl.FlexLayoutConfig;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

/**
 * Web UI configuration for {@link CentreConfigUpdater}.
 *
 * @author TG Team
 *
 */
public class CentreConfigurationWebUiConfig {
    public final EntityMaster<CentreConfigUpdater> centreConfigUpdater;
    public final EntityMaster<CentreConfigUpdaterDefaultAction> centreConfigUpdaterDefaultAction;
    public final EntityMaster<CentreColumnWidthConfigUpdater> centreColumnWidthConfigUpdater;
    public final EntityMaster<CentreConfigEditAction> centreConfigEditActionMaster;
    public final EntityMaster<CentreConfigLoadAction> centreConfigLoadActionMaster;
    public final EntityMaster<CentreConfigDeleteAction> centreConfigDeleteActionMaster;
    public final EntityMaster<OverrideCentreConfig> overrideCentreConfigMaster;
    
    public CentreConfigurationWebUiConfig(final Injector injector) {
        centreConfigUpdater = createCentreConfigUpdater(injector);
        centreConfigUpdaterDefaultAction = createCentreConfigUpdaterDefaultAction(injector);
        centreColumnWidthConfigUpdater = createCentreColumnWidthConfigUpdater(injector);
        centreConfigEditActionMaster = createCentreConfigEditActionMaster(injector);
        centreConfigLoadActionMaster = createCentreConfigLoadActionMaster(injector);
        centreConfigDeleteActionMaster = createCentreConfigDeleteActionMaster(injector);
        overrideCentreConfigMaster = createOverrideCentreConfigMaster(injector);
    }
    
    private static String createLayoutForCollectionalMaster() {
        return "['padding:20px', 'height: 100%', 'box-sizing: border-box', ['flex', ['flex']] ]";
    }
    
    /**
     * Creates entity master for {@link CentreConfigUpdater}.
     *
     * @return
     */
    private static EntityMaster<CentreConfigUpdater> createCentreConfigUpdater(final Injector injector) {
        final FlexLayoutConfig horizontal = layout().withClass("wrap").withStyle("padding", "20px 20px 0 20px").horizontal().justified().end();
        final String actionLayout = cell(
            cell(
                layout().withStyle("width", MASTER_ACTION_DEFAULT_WIDTH + "px").withStyle("margin-bottom", "20px").end()
            )
            .cell(
                cell().cell().layoutForEach(layout().withStyle("width", MASTER_ACTION_DEFAULT_WIDTH + "px").end()).withGapBetweenCells(20),
                layout().withStyle("margin-left", "auto").withStyle("margin-bottom", "20px").end()
            )
            .withGapBetweenCells(20),
            horizontal
        ).toString();
        final String layout = createLayoutForCollectionalMaster();
        final IMaster<CentreConfigUpdater> masterConfig = new SimpleMasterBuilder<CentreConfigUpdater>()
                .forEntity(CentreConfigUpdater.class)
                .addProp("customisableColumns").asCollectionalEditor().reorderable().withHeader("title")
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
                .addAction(REFRESH).shortDesc("CANCEL").longDesc("Cancel action")
                .addAction(SAVE).shortDesc("APPLY").longDesc("Apply columns customisation")
                .setActionBarLayoutFor(DESKTOP, empty(), actionLayout)
                .setActionBarLayoutFor(TABLET, empty(), actionLayout)
                .setActionBarLayoutFor(MOBILE, empty(), actionLayout)
                .setLayoutFor(DESKTOP, empty(), layout)
                .setLayoutFor(TABLET, empty(), layout)
                .setLayoutFor(MOBILE, empty(), layout)
                .withDimensions(mkDim("'30%'", "'50%'"))
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
        return new EntityMaster<CentreColumnWidthConfigUpdater>(CentreColumnWidthConfigUpdater.class, CentreColumnWidthConfigUpdaterProducer.class, null, injector);
    }
    
    public enum CentreConfigActions {
        CUSTOMISE_COLUMNS_ACTION {
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
                                    + "        self.$.egi._adjustColumns(functionalEntity.get('chosenIds').map(column => column === 'this' ? '' : column));\n"
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
    
    /**
     * Creates entity master for {@link CentreConfigEditAction}.
     *
     * @return
     */
    private static EntityMaster<CentreConfigEditAction> createCentreConfigEditActionMaster(final Injector injector) {
        final String actionLayout = mkActionLayoutForMaster();
        final String layout = mkGridForMasterFitWidth(2, 1);
        
        final IMaster<CentreConfigEditAction> masterConfig = new SimpleMasterBuilder<CentreConfigEditAction>()
            .forEntity(CentreConfigEditAction.class)
            .addProp("title").asSinglelineText().also()
            .addProp("desc").asMultilineText().also()
            .addAction(REFRESH).shortDesc("CANCEL").longDesc("Cancels creation of configuration copy.")
            .addAction(SAVE).shortDesc("SAVE").longDesc("Saves new configuration copy.")
            .setActionBarLayoutFor(DESKTOP, empty(), actionLayout)
            .setActionBarLayoutFor(TABLET, empty(), actionLayout)
            .setActionBarLayoutFor(MOBILE, empty(), actionLayout)
            .setLayoutFor(DESKTOP, empty(), layout)
            .setLayoutFor(TABLET, empty(), layout)
            .setLayoutFor(MOBILE, empty(), layout)
            .withDimensions(mkDim(400, 300))
            .done();
        return new EntityMaster<CentreConfigEditAction>(
            CentreConfigEditAction.class,
            CentreConfigEditActionProducer.class,
            masterConfig,
            injector);
    }
    
    /**
     * Creates entity master for {@link CentreConfigLoadAction}.
     *
     * @return
     */
    private static EntityMaster<CentreConfigLoadAction> createCentreConfigLoadActionMaster(final Injector injector) {
        final String actionLayout = mkActionLayoutForMaster();
        final String layout = createLayoutForCollectionalMaster();
        final IMaster<CentreConfigLoadAction> masterConfig = new SimpleMasterBuilder<CentreConfigLoadAction>()
            .forEntity(CentreConfigLoadAction.class)
            .addProp("centreConfigurations").asCollectionalEditor().also()
            .addAction(REFRESH).shortDesc("CANCEL").longDesc("Cancels configuration loading.")
            .addAction(SAVE).shortDesc("LOAD").longDesc("Loads currently chosen configuration.")
            .setActionBarLayoutFor(DESKTOP, empty(), actionLayout)
            .setActionBarLayoutFor(TABLET, empty(), actionLayout)
            .setActionBarLayoutFor(MOBILE, empty(), actionLayout)
            .setLayoutFor(DESKTOP, empty(), layout)
            .setLayoutFor(TABLET, empty(), layout)
            .setLayoutFor(MOBILE, empty(), layout)
            .withDimensions(mkDim("'30%'", "'50%'"))
            .done();
        return new EntityMaster<CentreConfigLoadAction>(
            CentreConfigLoadAction.class,
            CentreConfigLoadActionProducer.class,
            masterConfig,
            injector);
    }
    
    /**
     * Creates no-ui entity master for {@link CentreConfigDeleteAction}.
     *
     * @return
     */
    private static EntityMaster<CentreConfigDeleteAction> createCentreConfigDeleteActionMaster(final Injector injector) {
        return new EntityMaster<CentreConfigDeleteAction>(CentreConfigDeleteAction.class, CentreConfigDeleteActionProducer.class, null, injector);
    }
    
    private EntityMaster<OverrideCentreConfig> createOverrideCentreConfigMaster(final Injector injector) {
        final FlexLayoutConfig padding = layout().withStyle("padding", "20px").end();
        final String layout = cell( // TODO 
            cell(html("<div style='color: #737373; font-size: 13px; font-weight: bold; margin-top: -16px; margin-bottom: -16px;'>Configuration with this title already exists. Override?<div>"), padding),
            padding).toString();
        
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
                .done();
        
        return new EntityMaster<OverrideCentreConfig>(OverrideCentreConfig.class, masterConfig, injector);
    }
    
}