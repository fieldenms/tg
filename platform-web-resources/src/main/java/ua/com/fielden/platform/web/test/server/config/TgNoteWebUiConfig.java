package ua.com.fielden.platform.web.test.server.config;

import com.google.inject.Injector;
import ua.com.fielden.platform.sample.domain.TgNote;
import ua.com.fielden.platform.ui.menu.sample.MiTgEntityWithRichTextProp;
import ua.com.fielden.platform.ui.menu.sample.MiTgNote;
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

public class TgNoteWebUiConfig {

    private static final PrefDim RICH_TEXT_DIM = mkDim(960, 640);

    private static final FlexLayoutConfig FLEXIBLE_ROW = layout().flexAuto().end();
    private static final FlexLayoutConfig FLEXIBLE_LAYOUT_WITH_PADDING = layout()
            .withStyle("height", "100%")
            .withStyle("box-sizing", "border-box")
            .withStyle("min-height", "fit-content")
            .withStyle("padding", MARGIN_PIX).end();

    public final EntityCentre<TgNote> centre;
    public final EntityMaster<TgNote> master;

    public static TgNoteWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new TgNoteWebUiConfig(injector, builder);
    }

    private TgNoteWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        centre = createCentre(injector);
        builder.register(centre);
        master = createMaster(injector);
        builder.register(master);
    }

    /**
     * Creates entity centre for {@link TgNote}.
     *
     * @param injector
     * @return created entity centre
     */
    private EntityCentre<TgNote> createCentre(final Injector injector) {
        final String layout = LayoutComposer.mkGridForCentre(1, 2);

        final EntityActionConfig standardNewAction = StandardActions.NEW_ACTION.mkAction(TgNote.class);
        final EntityActionConfig standardDeleteAction = StandardActions.DELETE_ACTION.mkAction(TgNote.class);
        final EntityActionConfig standardExportAction = StandardActions.EXPORT_ACTION.mkAction(TgNote.class);
        final EntityActionConfig standardEditAction = StandardActions.EDIT_ACTION.mkAction(TgNote.class);
        final EntityActionConfig standardSortAction = CentreConfigurationWebUiConfig.CentreConfigActions.CUSTOMISE_COLUMNS_ACTION.mkAction();

        final EntityCentreConfig<TgNote> ecc = EntityCentreBuilder.centreFor(TgNote.class)
                .addTopAction(standardNewAction).also()
                .addTopAction(standardDeleteAction).also()
                .addTopAction(standardSortAction).also()
                .addTopAction(standardExportAction)
                .addCrit("this").asMulti().autocompleter(TgNote.class)/*.setDefaultValue(multi().string().setValues("RICH_TEXT_KEY1").value())*/.also()
                .addCrit("text").asMulti().text()/*.setDefaultValue(multi().string().setValues("key1").value())*/
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)

                .addProp("this").order(1).asc().width(100)
                    .withSummary("total_count_", "COUNT(SELF)", "Count:The total number of matching entities.")
                    .withAction(standardEditAction).also()
                .addProp("text").minWidth(400)
                .addPrimaryAction(standardEditAction)
                .build();

        final EntityCentre<TgNote> entityCentre = new EntityCentre<>(MiTgNote.class, "MiTgNote", ecc, injector, null);
        return entityCentre;
    }

    private EntityMaster<TgNote> createMaster(final Injector injector) {
        final String layout = cell(
                cell(cell(CELL_LAYOUT)).repeat(2),
            FLEXIBLE_LAYOUT_WITH_PADDING).toString();

        final IMaster<TgNote> masterConfig = new SimpleMasterBuilder<TgNote>().forEntity(TgNote.class)
                .addProp("text").asMultilineText().also()
                .addProp("key").asSinglelineText().also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
                .addAction(MasterActions.SAVE)
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), LayoutComposer.mkActionLayoutForMaster())
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .withDimensions(RICH_TEXT_DIM)
                .done();

        return new EntityMaster<TgNote>(TgNote.class, masterConfig, injector);
    }
}
