package ua.com.fielden.platform.web.test.server.config;

import com.google.inject.Injector;
import ua.com.fielden.platform.sample.domain.TgFuelType;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.ui.menu.sample.MiTgFuelUsage;
import ua.com.fielden.platform.web.PrefDim;
import ua.com.fielden.platform.web.action.CentreConfigurationWebUiConfig;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.layout.api.impl.FlexLayoutConfig;
import ua.com.fielden.platform.web.layout.api.impl.LayoutComposer;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

import java.util.Optional;

import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.cell;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutCellBuilder.layout;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.*;

public class TgFuelUsageWebUiConfig {

    private static final PrefDim TG_FUEL_USAGE_DIM = mkDim(960, 640);

    private static final FlexLayoutConfig FLEXIBLE_LAYOUT_WITH_PADDING = layout()
            .withStyle("height", "100%")
            .withStyle("box-sizing", "border-box")
            .withStyle("min-height", "fit-content")
            .withStyle("padding", MARGIN_PIX).end();

    public final EntityCentre<TgFuelUsage> centre;
    public final EntityMaster<TgFuelUsage> master;

    public static TgFuelUsageWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new TgFuelUsageWebUiConfig(injector, builder);
    }

    private TgFuelUsageWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        centre = createCentre(injector);
        builder.register(centre);
        master = createMaster(injector);
        builder.register(master);
    }

    private EntityCentre<TgFuelUsage> createCentre(final Injector injector) {
        final String layout = LayoutComposer.mkGridForCentre(3, 2);

        final var standardNewAction = StandardActions.NEW_ACTION.mkAction(TgFuelUsage.class);
        final var standardDeleteAction = StandardActions.DELETE_ACTION.mkAction(TgFuelUsage.class);
        final var standardExportAction = StandardActions.EXPORT_ACTION.mkAction(TgFuelUsage.class);
        final var standardEditAction = StandardActions.EDIT_ACTION.mkAction(TgFuelUsage.class);
        final var standardSortAction = CentreConfigurationWebUiConfig.CentreConfigActions.CUSTOMISE_COLUMNS_ACTION.mkAction();

        final var ecc = EntityCentreBuilder.centreFor(TgFuelUsage.class)
                .addTopAction(standardNewAction).also()
                .addTopAction(standardDeleteAction).also()
                .addTopAction(standardSortAction).also()
                .addTopAction(standardExportAction)
                .addCrit("vehicle").asMulti().autocompleter(TgVehicle.class).also()
                .addCrit("date").asRange().dateTime().also()
                .addCrit("qty").asRange().decimal().also()
                .addCrit("location").asMulti().text().also()
                .addCrit("fuelType").asMulti().autocompleter(TgFuelType.class).also()
                .addCrit("pricePerLitre").asRange().decimal()
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .addProp("this").order(1).asc().width(100)
                    .withSummary("total_count_", "COUNT(SELF)", "Count:The total number of matching entities.")
                    .withAction(standardEditAction).also()
                .addProp("vehicle").minWidth(100).also()
                .addProp("date").minWidth(100).also()
                .addProp("qty").minWidth(100).also()
                .addProp("location").minWidth(100).also()
                .addProp("fuelType").minWidth(100).also()
                .addProp("pricePerLitre").minWidth(100).also()
                .addProp("previousPricePerLitre").minWidth(100).also()
                .addProp("halfPricePerLitre").minWidth(100)
                .addPrimaryAction(standardEditAction)
                .build();

        return new EntityCentre<>(MiTgFuelUsage.class, ecc, injector);
    }

    private EntityMaster<TgFuelUsage> createMaster(final Injector injector) {
        final String layout = cell(
                cell(cell(CELL_LAYOUT).repeat(2).withGapBetweenCells(MARGIN))
                .cell(cell(CELL_LAYOUT).repeat(2).withGapBetweenCells(MARGIN))
                .cell(cell(CELL_LAYOUT).skip().withGapBetweenCells(MARGIN))
                .cell(cell(CELL_LAYOUT).repeat(2).withGapBetweenCells(MARGIN))
            , FLEXIBLE_LAYOUT_WITH_PADDING).toString();

        final var masterConfig = new SimpleMasterBuilder<TgFuelUsage>().forEntity(TgFuelUsage.class)
                .addProp("vehicle").asAutocompleter().also()
                .addProp("date").asDateTimePicker().also()
                .addProp("qty").asDecimal().also()
                .addProp("location").asSinglelineText().also()
                .addProp("fuelType").asAutocompleter().also()
                .addProp("pricePerLitreValidation").asSinglelineText().also()
                .addProp("pricePerLitre").asMoney().also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
                .addAction(MasterActions.SAVE)
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), LayoutComposer.mkActionLayoutForMaster())
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .withDimensions(TG_FUEL_USAGE_DIM)
                .done();

        return new EntityMaster<>(TgFuelUsage.class, masterConfig, injector);
    }
}
