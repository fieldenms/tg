package ua.com.fielden.platform.web.resources.webui;

import com.google.inject.Injector;
import ua.com.fielden.platform.dashboard.DashboardRefreshFrequency;
import ua.com.fielden.platform.dashboard.DashboardRefreshFrequencyUnit;
import ua.com.fielden.platform.ui.menu.sample.MiDashboardRefreshFrequency;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

import static java.util.Optional.empty;
import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.action.CentreConfigurationWebUiConfig.CentreConfigActions.CUSTOMISE_COLUMNS_ACTION;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.*;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.*;
import static ua.com.fielden.platform.web.test.server.config.StandardActions.*;
import static ua.com.fielden.platform.web.view.master.api.actions.MasterActions.REFRESH;
import static ua.com.fielden.platform.web.view.master.api.actions.MasterActions.SAVE;

///
/// Web UI configuration for entity [DashboardRefreshFrequency].
///
public class DashboardRefreshFrequencyWebUiConfig {

    public final EntityCentre<DashboardRefreshFrequency> centre;
    public final EntityMaster<DashboardRefreshFrequency> master;

    public static DashboardRefreshFrequencyWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new DashboardRefreshFrequencyWebUiConfig(injector, builder);
    }

    private DashboardRefreshFrequencyWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        centre = createCentre(injector);
        builder.register(centre);
        master = createMaster(injector);
        builder.register(master);
    }

    ///
    /// Creates an entity centre configuration for [DashboardRefreshFrequency].
    ///
    private EntityCentre<DashboardRefreshFrequency> createCentre(final Injector injector) {
        final String layout = mkGridForCentre(2, 1);

        final EntityActionConfig standardNewAction = NEW_ACTION.mkAction(DashboardRefreshFrequency.class);
        final EntityActionConfig standardDeleteAction = DELETE_ACTION.mkAction(DashboardRefreshFrequency.class);
        final EntityActionConfig standardExportAction = EXPORT_ACTION.mkAction(DashboardRefreshFrequency.class);
        final EntityActionConfig standardEditAction = EDIT_ACTION.mkAction(DashboardRefreshFrequency.class);
        final EntityActionConfig standardSortAction = CUSTOMISE_COLUMNS_ACTION.mkAction();

        final EntityCentreConfig<DashboardRefreshFrequency> ecc = EntityCentreBuilder.centreFor(DashboardRefreshFrequency.class).runAutomatically()
                .addFrontAction(standardNewAction)
                .addTopAction(standardNewAction).also()
                .addTopAction(standardDeleteAction).also()
                .addTopAction(standardSortAction).also()
                .addTopAction(standardExportAction)
                .addCrit("value").asRange().integer().also()
                .addCrit("refreshFrequencyUnit").asMulti().autocompleter(DashboardRefreshFrequencyUnit.class)
                .setLayoutFor(DESKTOP, empty(), layout)
                .setLayoutFor(TABLET, empty(), layout)
                .setLayoutFor(MOBILE, empty(), layout)
                .addProp("value").width(100)
                    .withAction(standardEditAction).also()
                .addProp("refreshFrequencyUnit").minWidth(100)
                    .withAction(standardEditAction).also()
                .addProp("millis").order(1).desc().width(100)
                .addPrimaryAction(standardEditAction)
                .build();

        return new EntityCentre<>(MiDashboardRefreshFrequency.class, ecc, injector);
    }

    ///
    /// Creates entity master for {@link DashboardRefreshFrequency}.
    ///
    private EntityMaster<DashboardRefreshFrequency> createMaster(final Injector injector) {
        final String layout = mkGridForMasterFitWidth(2, 1);

        final IMaster<DashboardRefreshFrequency> masterConfig = new SimpleMasterBuilder<DashboardRefreshFrequency>().forEntity(DashboardRefreshFrequency.class)
                .addProp("value").asInteger().also()
                .addProp("refreshFrequencyUnit").asAutocompleter().also()
                .addAction(REFRESH).shortDesc("Cancel").longDesc("Cancel changes")
                .addAction(SAVE).shortDesc("Save").longDesc("Save changes.")
                .setActionBarLayoutFor(DESKTOP, empty(), mkActionLayoutForMaster())
                .setLayoutFor(DESKTOP, empty(), layout)
                .setLayoutFor(TABLET, empty(), layout)
                .setLayoutFor(MOBILE, empty(), layout)
                .withDimensions(mkDim(380, 240))
                .done();

        return new EntityMaster<>(DashboardRefreshFrequency.class, masterConfig, injector);
    }

}
