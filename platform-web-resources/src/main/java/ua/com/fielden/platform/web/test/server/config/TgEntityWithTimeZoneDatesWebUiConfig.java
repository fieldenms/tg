package ua.com.fielden.platform.web.test.server.config;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.sample.domain.TgEntityWithTimeZoneDates;
import ua.com.fielden.platform.ui.menu.sample.MiTgEntityWithTimeZoneDates;
import ua.com.fielden.platform.web.action.CentreConfigurationWebUiConfig.CentreConfigActions;
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
/** 
 * {@link TgEntityWithTimeZoneDates} Web UI configuration.
 * 
 * @author Developers
 *
 */
public class TgEntityWithTimeZoneDatesWebUiConfig {

    public final EntityCentre<TgEntityWithTimeZoneDates> centre;
    public final EntityMaster<TgEntityWithTimeZoneDates> master;

    public static TgEntityWithTimeZoneDatesWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new TgEntityWithTimeZoneDatesWebUiConfig(injector, builder);
    }

    private TgEntityWithTimeZoneDatesWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        centre = createCentre(injector);
        builder.register(centre);
        master = createMaster(injector);
        builder.register(master);
    }

    /**
     * Creates entity centre for {@link TgEntityWithTimeZoneDates}.
     *
     * @param injector
     * @return created entity centre
     */
    private EntityCentre<TgEntityWithTimeZoneDates> createCentre(final Injector injector) {
        final String layout = LayoutComposer.mkGridForCentre(1, 1);

        final EntityActionConfig standardNewAction = StandardActions.NEW_ACTION.mkAction(TgEntityWithTimeZoneDates.class);
        final EntityActionConfig standardDeleteAction = StandardActions.DELETE_ACTION.mkAction(TgEntityWithTimeZoneDates.class);
        final EntityActionConfig standardExportAction = StandardActions.EXPORT_ACTION.mkAction(TgEntityWithTimeZoneDates.class);
        final EntityActionConfig standardEditAction = StandardActions.EDIT_ACTION.mkAction(TgEntityWithTimeZoneDates.class);
        final EntityActionConfig standardSortAction = CentreConfigActions.CUSTOMISE_COLUMNS_ACTION.mkAction();

        final EntityCentreConfig<TgEntityWithTimeZoneDates> ecc = EntityCentreBuilder.centreFor(TgEntityWithTimeZoneDates.class)
                .addTopAction(standardNewAction).also()
                .addTopAction(standardDeleteAction).also()
                .addTopAction(standardSortAction).also()
                .addTopAction(standardExportAction)
                .addCrit("this").asMulti().autocompleter(TgEntityWithTimeZoneDates.class)
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .addProp("this").order(1).asc().minWidth(100)
                    .withSummary("total_count_", "COUNT(SELF)", "Count:The total number of matching TgEntityWithTimeZoneDates.")
                    .withAction(standardEditAction).also()
                .addProp("dateProp").minWidth(100).also()
                .addProp("datePropUtc").minWidth(100)
                .addPrimaryAction(standardEditAction)
                .build();

        final EntityCentre<TgEntityWithTimeZoneDates> entityCentre = new EntityCentre<>(MiTgEntityWithTimeZoneDates.class, "MiTgEntityWithTimeZoneDates", ecc, injector, null);
        return entityCentre;
    }
    private EntityMaster<TgEntityWithTimeZoneDates> createMaster(final Injector injector) {
        final String layout = LayoutComposer.mkGridForMaster(640, 1, 2);

        final IMaster<TgEntityWithTimeZoneDates> masterConfig = new SimpleMasterBuilder<TgEntityWithTimeZoneDates>().forEntity(TgEntityWithTimeZoneDates.class)
                .addProp("dateProp").asDateTimePicker().also()
                .addProp("datePropUtc").asDateTimePicker().also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
                .addAction(MasterActions.SAVE)
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), LayoutComposer.mkActionLayoutForMaster())
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .done();

        return new EntityMaster<TgEntityWithTimeZoneDates>(TgEntityWithTimeZoneDates.class, masterConfig, injector);
    }
}