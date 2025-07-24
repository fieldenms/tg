package ua.com.fielden.platform.web.test.server.config;

import static ua.com.fielden.platform.web.action.pre.PreActions.okCancel;
import com.google.inject.Injector;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.EntityDeleteAction;
import ua.com.fielden.platform.entity.IContextDecomposer;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.sample.domain.producers.TgGeneratedEntityForTrippleDecAnalysisInsertionPointProducer;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.ui.menu.sample.MiTgGeneratedEntityForTrippleDecAnalysis;
import ua.com.fielden.platform.ui.menu.sample.MiTgOpenTrippleDecDetails;
import ua.com.fielden.platform.web.PrefDim.Unit;
import ua.com.fielden.platform.web.action.CentreConfigurationWebUiConfig.CentreConfigActions;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.impl.InsertionPointToolbar;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.layout.api.impl.LayoutComposer;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;
import ua.com.fielden.platform.web.view.master.api.with_centre.impl.MasterWithCentreBuilder;
import ua.com.fielden.platform.web.view.master.chart.decker.api.impl.ChartDeckerMasterBuilder;

import java.util.Optional;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.IContextDecomposer.decompose;
import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.action.pre.ConfirmationPreAction.okCancel;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.cell;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.CELL_LAYOUT;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.MARGIN;
import static ua.com.fielden.platform.web.test.server.config.StandardActionsStyles.STANDARD_ACTION_COLOUR;
import static ua.com.fielden.platform.web.test.server.config.StandardMessages.DELETE_CONFIRMATION;
import static ua.com.fielden.platform.web.view.master.chart.decker.api.LabelOrientation.VERTICAL;
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
        final EntityCentre<TgGeneratedEntityForTrippleDecAnalysis> detailsCentre = createDetailsCentre(injector);
        builder.register(detailsCentre);
        builder.register(createDetailsMaster(detailsCentre, injector));
    }

    private EntityMaster<TgOpenTrippleDecDetails> createDetailsMaster(final EntityCentre<TgGeneratedEntityForTrippleDecAnalysis> detailsCentre, final Injector injector) {
        final IMaster<TgOpenTrippleDecDetails> config = new MasterWithCentreBuilder<TgOpenTrippleDecDetails>()
                .forEntityWithSaveOnActivate(TgOpenTrippleDecDetails.class).withCentre(detailsCentre).done();

                return new EntityMaster<>(
                        TgOpenTrippleDecDetails.class,
                        config,
                        injector);
    }

    private EntityCentre<TgGeneratedEntityForTrippleDecAnalysis> createDetailsCentre(final Injector injector) {
        final EntityActionConfig standardNewAction = StandardActions.NEW_ACTION.mkAction(TgGeneratedEntityForTrippleDecAnalysis.class);
        final EntityActionConfig standardDeleteAction = StandardActions.DELETE_ACTION.mkAction(TgGeneratedEntityForTrippleDecAnalysis.class);
        final EntityActionConfig standardExportAction = StandardActions.EXPORT_ACTION.mkAction(TgGeneratedEntityForTrippleDecAnalysis.class);
        final EntityActionConfig standardEditAction = StandardActions.EDIT_ACTION.mkAction(TgGeneratedEntityForTrippleDecAnalysis.class);
        final EntityActionConfig standardSortAction = CentreConfigActions.CUSTOMISE_COLUMNS_ACTION.mkAction();


        final EntityCentreConfig<TgGeneratedEntityForTrippleDecAnalysis> ecc = EntityCentreBuilder.centreFor(TgGeneratedEntityForTrippleDecAnalysis.class)
                .runAutomatically()
                .addTopAction(standardNewAction).also()
                .addTopAction(standardDeleteAction).also()
                .addTopAction(standardSortAction).also()
                .addTopAction(standardExportAction)
                .setPageCapacity(10)
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
                .setQueryEnhancer(TgGeneratedEntityForTrippleDecAnalysisQueryEnhnacer.class, context().withMasterEntity().build())
                .build();

        final EntityCentre<TgGeneratedEntityForTrippleDecAnalysis> entityCentre = new EntityCentre<>(MiTgOpenTrippleDecDetails.class, "MiTgOpenTrippleDecDetails", ecc, injector, null);
        return entityCentre;
    }

    private static class TgGeneratedEntityForTrippleDecAnalysisQueryEnhnacer implements IQueryEnhancer<TgGeneratedEntityForTrippleDecAnalysis> {

          @Override
          public ICompleted<TgGeneratedEntityForTrippleDecAnalysis> enhanceQuery(final IWhere0<TgGeneratedEntityForTrippleDecAnalysis> where, final Optional<CentreContext<TgGeneratedEntityForTrippleDecAnalysis, ?>> context) {
              final IContextDecomposer decompContext = decompose(context);
              if (decompContext.contextNotEmpty() && decompContext.masterEntityNotEmpty()) {
                  final AbstractEntity<?> currentEntity = decompContext.ofMasterEntity().currentEntity();
                  return where.prop("id").eq().val(currentEntity);
              }
              return where.val(1).eq().val(1);
          }

    }

    private EntityMaster<TgGeneratedEntityForTrippleDecAnalysisInsertionPoint> createTripleDecInsertionPoint(final Injector injector) {
        final EntityActionConfig customAction = action(TgOpenTrippleDecDetails.class)
                .withContext(context().withMasterEntity().withCurrentEntity().build())
                .icon("icons:copyright")
                .shortDesc("Some Action")
                .longDesc("Some Action Description")
                .prefDimForView(mkDim(600, Unit.PX, 300, Unit.PX))
                .build();
        final IMaster<TgGeneratedEntityForTrippleDecAnalysisInsertionPoint> config = new ChartDeckerMasterBuilder<TgGeneratedEntityForTrippleDecAnalysisInsertionPoint>()
                .forEntityWithSaveOnActivation(TgGeneratedEntityForTrippleDecAnalysisInsertionPoint.class)
                    .groupKeyProp("group")
                    .groupDescProp("desc")
                    .addDeckFor(TgGeneratedEntityForTrippleDecAnalysis.class)
                        .showLegend()
                        .withTitle("Count")
                        .withXAxisTitle("Groups")
                        .withXAxisLabelOrientation(VERTICAL)
                        .withYAxisTitle("Number of Items")
                        .withSeries("count").colour(new Colour("82B1FF")).title("Number of items").action(customAction)
                        .withLine("count").colour(new Colour("82B1FF")).title("Number of items")
                        .also()
                    .addDeckFor(TgGeneratedEntityForTrippleDecAnalysis.class)
                        .showLegend()
                        .withTitle("Cost")
                        .withXAxisTitle("Groups")
                        .withYAxisTitle("Cost $")
                        .withSeries("cost").colour(new Colour("A7FFEB")).title("Cost").action(customAction)
                        .withLine("cost").colour(new Colour("A7FFEB")).title("Cost")
                        .also()
                    .addDeckFor(TgGeneratedEntityForTrippleDecAnalysis.class)
                        .showLegend()
                        .withTitle("Hours")
                        .withXAxisTitle("Groups")
                        .withYAxisTitle("Hours")
                        .withSeries("hours").colour(new Colour("B388FF")).title("Hours").action(customAction)
                        .withLine("hours").colour(new Colour("B388FF")).title("Hours")
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
        final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(TgGeneratedEntityForTrippleDecAnalysis.class).getKey();
        final String desc = format("Delete selected %s entities", entityTitle);
        final EntityActionConfig standardDeleteAction = action(EntityDeleteAction.class)
            .withContext(context().withSelectedEntities().build())
            .preAction(okCancel(DELETE_CONFIRMATION.msg))
            .postActionSuccess(() -> new JsCode("self.$.egi.clearPageSelection(); \n"))
            .postActionError(() -> new JsCode("self.currentPage();\n"))
            .icon("icons:remove-circle-outline")
            .withStyle(STANDARD_ACTION_COLOUR)
            .shortDesc(desc)
            .longDesc(desc)
            .shortcut("alt+d")
            .withNoInsertionPointsRefresh(TgGeneratedEntityForTrippleDecAnalysisInsertionPoint.class)
            .build();
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
                .addInsertionPoint(
                        action(TgGeneratedEntityForTrippleDecAnalysisInsertionPoint.class)
                             .withContext(context().withSelectionCrit().build())
                             .shortDesc("Triple decker analysis")
                             .prefDimForView(mkDim("'auto'", "'770px'"))
                             .withNoParentCentreRefresh()
                             .build(),
                         InsertionPoints.BOTTOM).setToolbar(new InsertionPointToolbar())
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

        return new EntityMaster<>(TgGeneratedEntityForTrippleDecAnalysis.class, masterConfig, injector);
    }
}