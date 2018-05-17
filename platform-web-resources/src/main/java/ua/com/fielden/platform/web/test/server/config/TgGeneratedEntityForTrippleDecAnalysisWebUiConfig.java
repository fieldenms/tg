package ua.com.fielden.platform.web.test.server.config;

import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.cell;
import static ua.com.fielden.platform.web.test.server.config.LayoutComposer.CELL_LAYOUT;
import static ua.com.fielden.platform.web.test.server.config.LayoutComposer.MARGIN;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.sample.domain.TgGeneratedEntity;
import ua.com.fielden.platform.sample.domain.TgGeneratedEntityForTrippleDecAnalysis;
import ua.com.fielden.platform.sample.domain.TgGeneratedEntityForTrippleDecAnalysisDao;
import ua.com.fielden.platform.sample.domain.TgGeneratedEntityForTrippleDecAnalysisInsertionPoint;
import ua.com.fielden.platform.sample.domain.producers.TgGeneratedEntityForTrippleDecAnalysisInsertionPointProducer;
import ua.com.fielden.platform.ui.menu.sample.MiTgGeneratedEntityForTrippleDecAnalysis;
import ua.com.fielden.platform.web.action.CentreConfigurationWebUiConfig.CentreConfigActions;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;
import ua.com.fielden.platform.web.view.master.api.with_master.impl.ChartDecMasterBuilder;
/**
 * {@link TgGeneratedEntityForTrippleDecAnalysis} Web UI configuration.
 *
 * @author TG Team
 *
 */
public class TgGeneratedEntityForTrippleDecAnalysisWebUiConfig {


    public static TgGeneratedEntityForTrippleDecAnalysisWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new TgGeneratedEntityForTrippleDecAnalysisWebUiConfig(injector, builder);
    }

    private TgGeneratedEntityForTrippleDecAnalysisWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        builder.register(createCentre(injector));
        builder.register(createMaster(injector));
        builder.register(createTripleDecInsertionPoint(injector));
    }

    private EntityMaster<TgGeneratedEntityForTrippleDecAnalysisInsertionPoint> createTripleDecInsertionPoint(final Injector injector) {
        final IMaster<TgGeneratedEntityForTrippleDecAnalysisInsertionPoint> config = new ChartDecMasterBuilder<TgGeneratedEntityForTrippleDecAnalysisInsertionPoint>()
                .forEntityWithSaveOnActivation(TgGeneratedEntityForTrippleDecAnalysisInsertionPoint.class)
                .done();
        return new EntityMaster<>(TgGeneratedEntityForTrippleDecAnalysisInsertionPoint.class, TgGeneratedEntityForTrippleDecAnalysisInsertionPointProducer.class, config, injector);
    }

    /**
     * Creates entity centre for {@link TgGeneratedEntity}.
     *
     * @param injector
     * @return created entity centre
     */
    private EntityCentre<TgGeneratedEntityForTrippleDecAnalysis> createCentre(final Injector injector) {
        final String layout = cell(
                cell(cell(CELL_LAYOUT).repeat(2).withGapBetweenCells(MARGIN))
               .cell(cell(CELL_LAYOUT).repeat(2).withGapBetweenCells(MARGIN))
               .cell(cell().skip().layoutForEach(CELL_LAYOUT).withGapBetweenCells(MARGIN))).toString();

        final EntityActionConfig standardNewAction = StandardActions.NEW_ACTION.mkAction(TgGeneratedEntityForTrippleDecAnalysis.class);
        final EntityActionConfig standardDeleteAction = StandardActions.DELETE_ACTION.mkAction(TgGeneratedEntityForTrippleDecAnalysis.class);
        final EntityActionConfig standardExportAction = StandardActions.EXPORT_ACTION.mkAction(TgGeneratedEntityForTrippleDecAnalysis.class);
        final EntityActionConfig standardEditAction = StandardActions.EDIT_ACTION.mkAction(TgGeneratedEntityForTrippleDecAnalysis.class);
        final EntityActionConfig standardSortAction = CentreConfigActions.CUSTOMISE_COLUMNS_ACTION.mkAction();

        final EntityCentreConfig<TgGeneratedEntityForTrippleDecAnalysis> ecc = EntityCentreBuilder.centreFor(TgGeneratedEntityForTrippleDecAnalysis.class)
                .addTopAction(standardNewAction).also()
                .addTopAction(standardDeleteAction).also()
                .addTopAction(standardSortAction).also()
                .addTopAction(standardExportAction)
                .addCrit("entityCount").asSingle().integer().also()
                .addCrit("group").asMulti().text().also()
                .addCrit("hours").asRange().decimal().also()
                .addCrit("cost").asRange().decimal().also()
                .addCrit("count").asRange().integer()
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .withGenerator(TgGeneratedEntityForTrippleDecAnalysis.class, TgGeneratedEntityForTrippleDecAnalysisDao.class)
                .setPageCapacity(20)
                .addProp("group").order(1).asc().width(100)
                    .withSummary("count_group_", "COUNT(SELF)", "The total number of generated entities.")
                    .withAction(standardEditAction).also()
                .addProp("desc").minWidth(400).also()
                .addProp("count").order(2).asc().minWidth(100)
                    .withSummary("sum_count_", "SUM(count)", "Sum of count property").also()
                .addProp("cost").minWidth(200)
                    .withSummary("sum_cost_", "SUM(cost)", "Sum of cost property").also()
                .addProp("hours").minWidth(60)
                    .withSummary("sum_hours_", "SUM(hours)", "Sum of hours property")
                .addPrimaryAction(standardEditAction)

                .addInsertionPointWithPagination(
                        action(TgGeneratedEntityForTrippleDecAnalysisInsertionPoint.class)
                             .withContext(context().withSelectionCrit().build())
                             .icon("stub")
                             .shortDesc("Triple decker analysis")
                             .prefDimForView(mkDim("'auto'", "'60px'"))
                             .withNoParentCentreRefresh()
                             .build(),
                         InsertionPoints.BOTTOM)
                .build();

        final EntityCentre<TgGeneratedEntityForTrippleDecAnalysis> entityCentre = new EntityCentre<>(MiTgGeneratedEntityForTrippleDecAnalysis.class, "MiTgGeneratedEntityForTrippleDecAnalysis", ecc, injector, null);
        return entityCentre;
    }
    private EntityMaster<TgGeneratedEntityForTrippleDecAnalysis> createMaster(final Injector injector) {
        final String layout = LayoutComposer.mkVarGridForMasterFitWidth(2, 2);

        final IMaster<TgGeneratedEntityForTrippleDecAnalysis> masterConfig = new SimpleMasterBuilder<TgGeneratedEntityForTrippleDecAnalysis>().forEntity(TgGeneratedEntityForTrippleDecAnalysis.class)
                .addProp("group").asSinglelineText().also()
                .addProp("count").asSpinner().also()
                .addProp("cost").asMoney().also()
                .addProp("hours").asDecimal().also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
                .addAction(MasterActions.SAVE)
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), LayoutComposer.mkActionLayoutForMaster())
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .done();

        return new EntityMaster<TgGeneratedEntityForTrippleDecAnalysis>(TgGeneratedEntityForTrippleDecAnalysis.class, masterConfig, injector);
    }
}