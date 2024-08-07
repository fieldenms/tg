package ua.com.fielden.platform.web.test.server.config;

import com.google.inject.Injector;
import ua.com.fielden.platform.sample.domain.TgEntityWithRichTextProp;
import ua.com.fielden.platform.ui.menu.sample.MiTgEntityWithRichTextProp;
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

import static java.lang.String.format;
import static ua.com.fielden.platform.web.action.StandardMastersWebUiConfig.MASTER_ACTION_SPECIFICATION;

public class TgEntityWithRichTextPropWebUiConfig {


    public final EntityCentre<TgEntityWithRichTextProp> centre;
    public final EntityMaster<TgEntityWithRichTextProp> master;

    public static TgEntityWithRichTextPropWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new TgEntityWithRichTextPropWebUiConfig(injector, builder);
    }

    private TgEntityWithRichTextPropWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        centre = createCentre(injector);
        builder.register(centre);
        master = createMaster(injector);
        builder.register(master);
    }

    /**
     * Creates entity centre for {@link TgEntityWithRichTextProp}.
     *
     * @param injector
     * @return created entity centre
     */
    private EntityCentre<TgEntityWithRichTextProp> createCentre(final Injector injector) {
        final String layout = LayoutComposer.mkGridForCentre(1, 2);

        final EntityActionConfig standardNewAction = StandardActions.NEW_ACTION.mkAction(TgEntityWithRichTextProp.class);
        final EntityActionConfig standardDeleteAction = StandardActions.DELETE_ACTION.mkAction(TgEntityWithRichTextProp.class);
        final EntityActionConfig standardExportAction = StandardActions.EXPORT_ACTION.mkAction(TgEntityWithRichTextProp.class);
        final EntityActionConfig standardEditAction = StandardActions.EDIT_ACTION.mkAction(TgEntityWithRichTextProp.class);
        final EntityActionConfig standardSortAction = CentreConfigurationWebUiConfig.CentreConfigActions.CUSTOMISE_COLUMNS_ACTION.mkAction();

        final EntityCentreConfig<TgEntityWithRichTextProp> ecc = EntityCentreBuilder.centreFor(TgEntityWithRichTextProp.class)
                .addTopAction(standardNewAction).also()
                .addTopAction(standardDeleteAction).also()
                .addTopAction(standardSortAction).also()
                .addTopAction(standardExportAction)
                .addCrit("this").asMulti().autocompleter(TgEntityWithRichTextProp.class).also()
                .addCrit("richTextProp").asMulti().text()
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)

                .addProp("this").order(1).asc().width(100)
                    .withSummary("total_count_", "COUNT(SELF)", "Count:The total number of matching TgGeneratedEntity.")
                    .withAction(standardEditAction).also()
                .addProp("desc").minWidth(400).also()
                .addProp("richTextProp").minWidth(400)
                .addPrimaryAction(standardEditAction)
                .build();

        final EntityCentre<TgEntityWithRichTextProp> entityCentre = new EntityCentre<>(MiTgEntityWithRichTextProp.class, "MiTgEntityWithRichTextProp", ecc, injector, null);
        return entityCentre;
    }
    private EntityMaster<TgEntityWithRichTextProp> createMaster(final Injector injector) {
        final String layout = LayoutComposer.mkGridForMasterFitWidth(3, 1);

        final IMaster<TgEntityWithRichTextProp> masterConfig = new SimpleMasterBuilder<TgEntityWithRichTextProp>().forEntity(TgEntityWithRichTextProp.class)
                .addProp("key").asSinglelineText().also()
                .addProp("desc").asSinglelineText().also()
                .addProp("richTextProp").asMultilineText().also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
                .addAction(MasterActions.SAVE)
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), LayoutComposer.mkActionLayoutForMaster())
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .done();

        return new EntityMaster<TgEntityWithRichTextProp>(TgEntityWithRichTextProp.class, masterConfig, injector);
    }
}
