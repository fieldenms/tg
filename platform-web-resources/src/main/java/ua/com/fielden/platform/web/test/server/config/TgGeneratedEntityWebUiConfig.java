package ua.com.fielden.platform.web.test.server.config;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.sample.domain.TgGeneratedEntity;
import ua.com.fielden.platform.sample.domain.TgGeneratedEntityGenerator;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.menu.sample.MiTgGeneratedEntity;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.resources.webui.CentreConfigurationWebUiConfig.CentreConfigActions;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;
/** 
 * {@link TgGeneratedEntity} Web UI configuration.
 * 
 * @author TG Team
 *
 */
public class TgGeneratedEntityWebUiConfig {

    public final EntityCentre<TgGeneratedEntity> centre;
    public final EntityMaster<TgGeneratedEntity> master;

    public static TgGeneratedEntityWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new TgGeneratedEntityWebUiConfig(injector, builder);
    }

    private TgGeneratedEntityWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        centre = createCentre(injector);
        builder.register(centre);
        master = createMaster(injector);
        builder.register(master);
    }

    /**
     * Creates entity centre for {@link TgGeneratedEntity}.
     *
     * @param injector
     * @return created entity centre
     */
    private EntityCentre<TgGeneratedEntity> createCentre(final Injector injector) {
        final String layout = LayoutComposer.mkGridForCentre(2, 2);

        final EntityActionConfig standardNewAction = StandardActions.NEW_ACTION.mkAction(TgGeneratedEntity.class);
        final EntityActionConfig standardDeleteAction = StandardActions.DELETE_ACTION.mkAction(TgGeneratedEntity.class);
        final EntityActionConfig standardExportAction = StandardActions.EXPORT_ACTION.mkAction(TgGeneratedEntity.class);
        final EntityActionConfig standardEditAction = StandardActions.EDIT_ACTION.mkAction(TgGeneratedEntity.class);
        final EntityActionConfig standardSortAction = CentreConfigActions.SORT_ACTION.mkAction();

        final EntityCentreConfig<TgGeneratedEntity> ecc = EntityCentreBuilder.centreFor(TgGeneratedEntity.class)
                .addTopAction(standardNewAction).also()
                .addTopAction(standardDeleteAction).also()
                .addTopAction(standardSortAction).also()
                .addTopAction(standardExportAction)
                .addCrit("this").asMulti().autocompleter(TgGeneratedEntity.class).also()
                .addCrit("critOnlyMultiProp").asMulti().autocompleter(User.class).also()
                .addCrit("critOnlySingleProp").asSingle().autocompleter(User.class).also()
                .addCrit("createdBy").asMulti().autocompleter(User.class)
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .withGenerator(TgGeneratedEntity.class, TgGeneratedEntityGenerator.class)
                .addProp("entityKey").order(1).asc().width(100)
                    .withSummary("total_count_", "COUNT(SELF)", "Count:The total number of matching TgGeneratedEntity.")
                    .withAction(standardEditAction).also()
                .addProp("desc").minWidth(400).also()
                .addProp("createdBy").minWidth(60)
                .addPrimaryAction(standardEditAction)
                .build();

        final EntityCentre<TgGeneratedEntity> entityCentre = new EntityCentre<>(MiTgGeneratedEntity.class, "MiTgGeneratedEntity", ecc, injector, null);
        return entityCentre;
    }
    private EntityMaster<TgGeneratedEntity> createMaster(final Injector injector) {
        final String layout = LayoutComposer.mkGridForMaster(640, 2, 1);

        final IMaster<TgGeneratedEntity> masterConfig = new SimpleMasterBuilder<TgGeneratedEntity>().forEntity(TgGeneratedEntity.class)
                .addProp("entityKey").asSinglelineText().also()
                .addProp("desc").asMultilineText().also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
                .addAction(MasterActions.SAVE)
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), LayoutComposer.mkActionLayoutForMaster())
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .done();

        return new EntityMaster<TgGeneratedEntity>(TgGeneratedEntity.class, masterConfig, injector);
    }
}