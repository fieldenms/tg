package ua.com.fielden.platform.web.test.server.config;

import com.google.inject.Injector;
import ua.com.fielden.platform.sample.domain.TgEntityWithRichTextProp;
import ua.com.fielden.platform.sample.domain.TgEntityWithRichTextRef;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.ui.menu.sample.MiTgEntityWithRichTextRef;
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

import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.cell;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutCellBuilder.layout;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.CELL_LAYOUT;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.MARGIN_PIX;

public class TgEntityWithRichTextRefWebUiConfig {

    private static final PrefDim RICH_TEXT_DIM = mkDim(960, 640);

    private static final FlexLayoutConfig FLEXIBLE_ROW = layout().flexAuto().end();
    private static final FlexLayoutConfig FLEXIBLE_LAYOUT_WITH_PADDING = layout()
            .withStyle("height", "100%")
            .withStyle("box-sizing", "border-box")
            .withStyle("min-height", "fit-content")
            .withStyle("padding", MARGIN_PIX).end();

    public final EntityCentre<TgEntityWithRichTextRef> centre;
    public final EntityMaster<TgEntityWithRichTextRef> master;

    public static TgEntityWithRichTextRefWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new TgEntityWithRichTextRefWebUiConfig(injector, builder);
    }

    private TgEntityWithRichTextRefWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        centre = createCentre(injector);
        builder.register(centre);
        master = createMaster(injector);
        builder.register(master);
    }

    /**
     * Creates entity centre for {@link TgEntityWithRichTextRef}.
     *
     * @param injector
     * @return created entity centre
     */
    private EntityCentre<TgEntityWithRichTextRef> createCentre(final Injector injector) {
        final String layout = LayoutComposer.mkGridForCentre(1, 2);

        final EntityActionConfig standardNewAction = StandardActions.NEW_ACTION.mkAction(TgEntityWithRichTextRef.class);
        final EntityActionConfig standardExportAction = StandardActions.EXPORT_ACTION.mkAction(TgEntityWithRichTextRef.class);
        final EntityActionConfig standardEditAction = StandardActions.EDIT_ACTION.mkAction(TgEntityWithRichTextRef.class);
        final EntityActionConfig standardSortAction = CentreConfigurationWebUiConfig.CentreConfigActions.CUSTOMISE_COLUMNS_ACTION.mkAction();

        final EntityCentreConfig<TgEntityWithRichTextRef> ecc = EntityCentreBuilder.centreFor(TgEntityWithRichTextRef.class)
                .addTopAction(standardNewAction).also()
                .addTopAction(standardSortAction).also()
                .addTopAction(standardExportAction)
                .addCrit("this").asMulti().autocompleter(TgEntityWithRichTextRef.class).also()
                .addCrit("richTextRef").asMulti().autocompleter(TgEntityWithRichTextProp.class).withProps(T2.t2("desc", false), T2.t2("richTextProp", false))
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)

                .addProp("this").order(1).asc().width(100)
                    .withSummary("total_count_", "COUNT(SELF)", "Count:The total number of matching TgGeneratedEntity.")
                    .withAction(standardEditAction).also()
                .addProp("desc").minWidth(400).also()
                .addProp("richTextRef").minWidth(400)
                .addPrimaryAction(standardEditAction)
                .build();

        final EntityCentre<TgEntityWithRichTextRef> entityCentre = new EntityCentre<>(MiTgEntityWithRichTextRef.class, "MiTgEntityWithRichTextRef", ecc, injector, null);
        return entityCentre;
    }

    private EntityMaster<TgEntityWithRichTextRef> createMaster(final Injector injector) {
        final String layout = cell(
                cell(cell(CELL_LAYOUT)).repeat(3),
            FLEXIBLE_LAYOUT_WITH_PADDING).toString();

        final IMaster<TgEntityWithRichTextRef> masterConfig = new SimpleMasterBuilder<TgEntityWithRichTextRef>().forEntity(TgEntityWithRichTextRef.class)
                .addProp("key").asSinglelineText().also()
                .addProp("desc").asSinglelineText().also()
                .addProp("richTextRef").asAutocompleter().withProps(T2.t2("desc", false), T2.t2("richTextProp", false)).also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
                .addAction(MasterActions.SAVE)
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), LayoutComposer.mkActionLayoutForMaster())
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .withDimensions(RICH_TEXT_DIM)
                .done();

        return new EntityMaster<TgEntityWithRichTextRef>(TgEntityWithRichTextRef.class, masterConfig, injector);
    }
}
