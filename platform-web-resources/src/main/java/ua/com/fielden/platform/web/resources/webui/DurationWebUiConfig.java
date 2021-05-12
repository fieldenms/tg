package ua.com.fielden.platform.web.resources.webui;

import static java.util.Optional.empty;
import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.action.CentreConfigurationWebUiConfig.CentreConfigActions.CUSTOMISE_COLUMNS_ACTION;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.MOBILE;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.TABLET;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.MARGIN;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.mkActionLayoutForMaster;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.mkGridForCentre;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.mkGridForMasterFitWidth;
import static ua.com.fielden.platform.web.test.server.config.StandardActions.DELETE_ACTION;
import static ua.com.fielden.platform.web.test.server.config.StandardActions.EDIT_ACTION;
import static ua.com.fielden.platform.web.test.server.config.StandardActions.EXPORT_ACTION;
import static ua.com.fielden.platform.web.test.server.config.StandardActions.NEW_ACTION;
import static ua.com.fielden.platform.web.view.master.api.actions.MasterActions.REFRESH;
import static ua.com.fielden.platform.web.view.master.api.actions.MasterActions.SAVE;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.Duration;
import ua.com.fielden.platform.entity.DurationUnit;
import ua.com.fielden.platform.ui.menu.sample.MiDuration;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

/**
 * {@link Duration} Web UI configuration.
 *
 * @author TG Team
 *
 */
public class DurationWebUiConfig {

    public final EntityCentre<Duration> centre;
    public final EntityMaster<Duration> master;

    public static DurationWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new DurationWebUiConfig(injector, builder);
    }

    private DurationWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        centre = createCentre(injector);
        builder.register(centre);
        master = createMaster(injector);
        builder.register(master);
    }

    /**
     * Creates entity centre for {@link Duration}.
     *
     * @param injector
     * @return created entity centre
     */
    private EntityCentre<Duration> createCentre(final Injector injector) {
        final String layout = mkGridForCentre(1, 2);

        final EntityActionConfig standardNewAction = NEW_ACTION.mkAction(Duration.class);
        final EntityActionConfig standardDeleteAction = DELETE_ACTION.mkAction(Duration.class);
        final EntityActionConfig standardExportAction = EXPORT_ACTION.mkAction(Duration.class);
        final EntityActionConfig standardEditAction = EDIT_ACTION.mkAction(Duration.class);
        final EntityActionConfig standardSortAction = CUSTOMISE_COLUMNS_ACTION.mkAction();

        final EntityCentreConfig<Duration> ecc = EntityCentreBuilder.centreFor(Duration.class).runAutomatically()
                .addFrontAction(standardNewAction)
                .addTopAction(standardNewAction).also()
                .addTopAction(standardDeleteAction).also()
                .addTopAction(standardSortAction).also()
                .addTopAction(standardExportAction)
                .addCrit("count").asRange().integer().also()
                .addCrit("durationUnit").asMulti().autocompleter(DurationUnit.class)
                .setLayoutFor(DESKTOP, empty(), layout)
                .setLayoutFor(TABLET, empty(), layout)
                .setLayoutFor(MOBILE, empty(), layout)
                .addProp("count").width(100)
                    .withAction(standardEditAction).also()
                .addProp("durationUnit").minWidth(100)
                    .withAction(standardEditAction).also()
                .addProp("millis").order(1).desc().width(0)
                .addPrimaryAction(standardEditAction)
                .build();

        final EntityCentre<Duration> entityCentre = new EntityCentre<>(MiDuration.class, "MiDuration", ecc, injector, null);
        return entityCentre;
    }

    /**
     * Creates entity master for {@link Duration}.
     *
     * @param injector
     * @return created entity master
     */
    private EntityMaster<Duration> createMaster(final Injector injector) {
        final String layout = mkGridForMasterFitWidth(1, 2);

        final IMaster<Duration> masterConfig = new SimpleMasterBuilder<Duration>().forEntity(Duration.class)
                .addProp("count").asSpinner().also()
                .addProp("durationUnit").asAutocompleter().also()
                .addAction(REFRESH).shortDesc("Cancel").longDesc("Cancel action")
                .addAction(SAVE)
                .setActionBarLayoutFor(DESKTOP, empty(), mkActionLayoutForMaster())
                .setLayoutFor(DESKTOP, empty(), layout)
                .setLayoutFor(TABLET, empty(), layout)
                .setLayoutFor(MOBILE, empty(), layout)
                .withDimensions(mkDim(480 + 2 * MARGIN, 260))
                .done();

        return new EntityMaster<>(Duration.class, masterConfig, injector);
    }
}