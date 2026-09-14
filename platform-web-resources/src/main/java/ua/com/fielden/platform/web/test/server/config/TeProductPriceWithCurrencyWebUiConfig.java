package ua.com.fielden.platform.web.test.server.config;

import com.google.inject.Injector;
import ua.com.fielden.platform.sample.domain.TeProductPriceWithCurrency;
import ua.com.fielden.platform.ui.menu.sample.MiTeProductPriceWithCurrency;
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

public class TeProductPriceWithCurrencyWebUiConfig {

    private static final PrefDim TG_FUEL_USAGE_DIM = mkDim(960, 640);

    private static final FlexLayoutConfig FLEXIBLE_LAYOUT_WITH_PADDING = layout()
            .withStyle("height", "100%")
            .withStyle("box-sizing", "border-box")
            .withStyle("min-height", "fit-content")
            .withStyle("padding", MARGIN_PIX).end();

    public final EntityCentre<TeProductPriceWithCurrency> centre;
    public final EntityMaster<TeProductPriceWithCurrency> master;

    public static TeProductPriceWithCurrencyWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new TeProductPriceWithCurrencyWebUiConfig(injector, builder);
    }

    private TeProductPriceWithCurrencyWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        centre = createCentre(injector);
        builder.register(centre);
        master = createMaster(injector);
        builder.register(master);
    }

    private EntityCentre<TeProductPriceWithCurrency> createCentre(final Injector injector) {
        final String layout = LayoutComposer.mkVarGridForCentre(2, 1);

        final var standardNewAction = StandardActions.NEW_ACTION.mkAction(TeProductPriceWithCurrency.class);
        final var standardDeleteAction = StandardActions.DELETE_ACTION.mkAction(TeProductPriceWithCurrency.class);
        final var standardExportAction = StandardActions.EXPORT_ACTION.mkAction(TeProductPriceWithCurrency.class);
        final var standardEditAction = StandardActions.EDIT_ACTION.mkAction(TeProductPriceWithCurrency.class);
        final var standardSortAction = CentreConfigurationWebUiConfig.CentreConfigActions.CUSTOMISE_COLUMNS_ACTION.mkAction();

        final var ecc = EntityCentreBuilder.centreFor(TeProductPriceWithCurrency.class)
                .addTopAction(standardNewAction).also()
                .addTopAction(standardDeleteAction).also()
                .addTopAction(standardSortAction).also()
                .addTopAction(standardExportAction)
                .addCrit("product").asMulti().text().also()
                .addCrit("price").asRange().decimal().also()
                .addCrit("other").asMulti().autocompleter(TeProductPriceWithCurrency.class)
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .addProp("this").order(1).asc().width(100)
                    .withSummary("total_count_", "COUNT(SELF)", "Count:The total number of matching entities.")
                    .withAction(standardEditAction).also()
                .addProp("product").minWidth(100).also()
                .addProp("price").minWidth(100).also()
                .addProp("other").minWidth(100)
                .addPrimaryAction(standardEditAction)
                .build();

        return new EntityCentre<>(MiTeProductPriceWithCurrency.class, ecc, injector);
    }

    private EntityMaster<TeProductPriceWithCurrency> createMaster(final Injector injector) {
        final String layout = cell(
                cell(cell(CELL_LAYOUT).repeat(2).withGapBetweenCells(MARGIN))
                .cell(cell(CELL_LAYOUT).skip().withGapBetweenCells(MARGIN))
            , FLEXIBLE_LAYOUT_WITH_PADDING).toString();

        final var masterConfig = new SimpleMasterBuilder<TeProductPriceWithCurrency>().forEntity(TeProductPriceWithCurrency.class)
                .addProp("product").asSinglelineText().also()
                .addProp("price").asMoney().also()
                .addProp("other").asAutocompleter().also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
                .addAction(MasterActions.SAVE)
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), LayoutComposer.mkActionLayoutForMaster())
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .withDimensions(TG_FUEL_USAGE_DIM)
                .done();

        return new EntityMaster<>(TeProductPriceWithCurrency.class, masterConfig, injector);
    }
}
