package ua.com.fielden.platform.web.test.server.config;

import com.google.inject.Injector;
import ua.com.fielden.platform.sample.domain.TgEntityWithRichTextProp;
import ua.com.fielden.platform.ui.menu.sample.MiTgEntityWithRichTextProp;
import ua.com.fielden.platform.web.PrefDim;
import ua.com.fielden.platform.web.action.CentreConfigurationWebUiConfig;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.layout.api.impl.FlexLayoutConfig;
import ua.com.fielden.platform.web.layout.api.impl.LayoutComposer;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

import java.util.Optional;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.options.DefaultValueOptions.multi;
import static ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.options.DefaultValueOptions.single;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.cell;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutCellBuilder.layout;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.*;

public class TgEntityWithRichTextPropWebUiConfig {

    private static final PrefDim RICH_TEXT_DIM = mkDim(960, 640);

    private static final FlexLayoutConfig FLEXIBLE_ROW = layout().flexAuto().end();
    private static final FlexLayoutConfig FLEXIBLE_LAYOUT_WITH_PADDING = layout()
            .withStyle("height", "100%")
            .withStyle("box-sizing", "border-box")
            .withStyle("min-height", "fit-content")
            .withStyle("padding", MARGIN_PIX).end();

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
                .addCrit("this").asMulti().autocompleter(TgEntityWithRichTextProp.class)/*.setDefaultValue(multi().string().setValues("RICH_TEXT_KEY1").value())*/.also()
                .addCrit("richTextProp").asMulti().text()/*.setDefaultValue(multi().string().setValues("key1").value())*/
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
        final String layout = cell(
                cell(cell(CELL_LAYOUT)).repeat(3),
            FLEXIBLE_LAYOUT_WITH_PADDING).toString();

        final IMaster<TgEntityWithRichTextProp> masterConfig = new SimpleMasterBuilder<TgEntityWithRichTextProp>().forEntity(TgEntityWithRichTextProp.class)
                .addProp("richTextProp").asRichText().withHeight(350).also()
                .addProp("key").asSinglelineText().also()
                .addProp("desc").asSinglelineText().also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
                .addAction(MasterActions.SAVE)
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), LayoutComposer.mkActionLayoutForMaster())
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .withDimensions(RICH_TEXT_DIM)
                .done();

        return new EntityMaster<TgEntityWithRichTextProp>(TgEntityWithRichTextProp.class, masterConfig, injector);
    }
}
