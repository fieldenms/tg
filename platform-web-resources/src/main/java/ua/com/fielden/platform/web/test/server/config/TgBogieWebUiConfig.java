package ua.com.fielden.platform.web.test.server.config;

import com.google.inject.Injector;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.ui.menu.sample.MiTgBogie;
import ua.com.fielden.platform.web.action.CentreConfigurationWebUiConfig;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.layout.api.impl.LayoutComposer;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

import java.util.Optional;

import static ua.com.fielden.platform.sample.domain.TgBogie.LOCATION;
import static ua.com.fielden.platform.web.PrefDim.mkDim;

/// Entity centre and master for [TgBogie], whose property `location` is of union type `TgBogieLocation`.
/// They demonstrate how an entity editor displays a union value, in its input layer and its tooltip.
///
public class TgBogieWebUiConfig {

    public final EntityCentre<TgBogie> centre;
    public final EntityMaster<TgBogie> master;

    public static TgBogieWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new TgBogieWebUiConfig(injector, builder);
    }

    private TgBogieWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        centre = createCentre(injector);
        builder.register(centre);
        master = createMaster(injector);
        builder.register(master);
    }

    private EntityCentre<TgBogie> createCentre(final Injector injector) {
        final String layout = LayoutComposer.mkGridForCentre(1, 2);

        final EntityActionConfig standardNewAction = StandardActions.NEW_ACTION.mkAction(TgBogie.class);
        final EntityActionConfig standardDeleteAction = StandardActions.DELETE_ACTION.mkAction(TgBogie.class);
        final EntityActionConfig standardEditAction = StandardActions.EDIT_ACTION.mkAction(TgBogie.class);
        final EntityActionConfig standardSortAction = CentreConfigurationWebUiConfig.CentreConfigActions.CUSTOMISE_COLUMNS_ACTION.mkAction();

        final EntityCentreConfig<TgBogie> ecc = EntityCentreBuilder.centreFor(TgBogie.class)
                .addTopAction(standardNewAction).also()
                .addTopAction(standardDeleteAction).also()
                .addTopAction(standardSortAction)
                .addCrit("this").asMulti().autocompleter(TgBogie.class).also()
                .addCrit("desc").asMulti().text()
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)

                .addProp("this").order(1).asc().width(100)
                    .withSummary("total_count_", "COUNT(SELF)", "Count:The total number of matching bogies.")
                    .withAction(standardEditAction).also()
                .addProp("desc").minWidth(200).also()
                .addProp(LOCATION).minWidth(200)
                .addPrimaryAction(standardEditAction)
                .build();

        return new EntityCentre<>(MiTgBogie.class, ecc, injector);
    }

    private EntityMaster<TgBogie> createMaster(final Injector injector) {
        final String layout = LayoutComposer.mkGridForMasterFitWidth(3, 1);

        final IMaster<TgBogie> masterConfig = new SimpleMasterBuilder<TgBogie>().forEntity(TgBogie.class)
                .addProp("key").asSinglelineText().also()
                .addProp("desc").asSinglelineText().also()
                .addProp(LOCATION).asAutocompleter().also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
                .addAction(MasterActions.SAVE)
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), LayoutComposer.mkActionLayoutForMaster())
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .withDimensions(mkDim(640, 360))
                .done();

        return new EntityMaster<>(TgBogie.class, masterConfig, injector);
    }
}
