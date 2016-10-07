package ua.com.fielden.platform.web.test.server;

import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.sample.domain.TgOrgUnit;
import ua.com.fielden.platform.sample.domain.TgStop;
import ua.com.fielden.platform.sample.domain.TgStopMap;
import ua.com.fielden.platform.ui.menu.sample.MiTgStop;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints;
import ua.com.fielden.platform.web.centre.api.resultset.scrolling.impl.ScrollConfig;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
/** 
 * {@link TgStop} Web UI configuration.
 * 
 * @author Developers
 *
 */
public class TgStopWebUiConfig {

    public final EntityCentre<TgStop> centre;

    public static TgStopWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new TgStopWebUiConfig(injector, builder);
    }

    private TgStopWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        centre = createCentre(injector);
        builder.register(centre);
        
        builder.register(createTgStopMapMaster(injector));
    }

    /**
     * Creates entity centre for {@link TgStop}.
     *
     * @param injector
     * @return created entity centre
     */
    private EntityCentre<TgStop> createCentre(final Injector injector) {
        final EntityCentreConfig<TgStop> centre = EntityCentreBuilder.centreFor(TgStop.class)
                .runAutomatically()
                .addCrit("machine").asMulti().autocompleter(TgMachine.class).also()
                .addCrit("orgUnit").asMulti().autocompleter(TgOrgUnit.class).also()
                .addCrit("gpsTime").asRange().date().also()
                .addCrit("durationInMinutes").asRange().decimal().also()
                .addCrit("radiusThreshould").asSingle().decimal().also()
                .addCrit("speedThreshould").asSingle().decimal().also()
                .addCrit("nightStop").asMulti().bool()
//                cdtme.getFirstTick().setValue(root(), "machine", entityVal("22763ТА"))
//                .setValue(root(), "gpsTime", dateVal("2013-12-01 00:00:00")).setValue2(root(), "gpsTime", dateVal("2013-12-20 23:59:59"))
//                .setValue(root(), "durationInMinutes", new BigDecimal(60.0))
//                .setValue(root(), "radiusThreshould", new BigDecimal(500.0))
//                .setValue(root(), "speedThreshould", new BigDecimal(20.0));
                
                
                .setLayoutFor(Device.DESKTOP, Optional.empty(), "[['center-justified', 'start', ['margin-right: 40px', 'flex'], ['flex']], ['center-justified', 'start', ['margin-right: 40px', 'flex'], ['flex']],['center-justified', 'start', ['margin-right: 40px', 'flex'], ['flex']],['center-justified', 'start', ['margin-right: 40px', 'flex'], ['flex']]]")
                .withScrollingConfig(ScrollConfig.configScroll().withFixedHeader().withFixedSummary().done())
                .setPageCapacity(10000)
                .setVisibleRowsCount(10)

                .addProp("machineResult")
                    // .order(1).asc()
                    .width(90)
                .also()
                .addProp("orgUnitResult")
                    // .order(2).asc()
                    .width(90)
                .also()
                .addProp("periodString")
                    .width(150)
                .also()
                .addProp("stopTimeFrom")
                    .width(150)
                .also()
                .addProp("nightStopResult")
                    .width(90)
                .also()
                .addProp("stopTimeTo")
                    .width(150)
                .also()
                .addProp("radius")
                    .width(150)
                .also()
                .addProp("distance")
                    .width(150)
                // TODO .setRenderingCustomiser(TgMessageRenderingCustomiser.class)
                .addInsertionPoint(
                    action(TgStopMap.class)
                            .withContext(context().withSelectionCrit().build())
                            .icon("credit-card")
                            .shortDesc("TgStop map")
                            .withNoParentCentreRefresh()
                            .build(),
                    InsertionPoints.BOTTOM
                )
                .build();

        final EntityCentre<TgStop> entityCentre = new EntityCentre<>(MiTgStop.class, "MiTgStop", centre, injector, null);
        return entityCentre;
    }
    
    private static EntityMaster<TgStopMap> createTgStopMapMaster(final Injector injector) {
        final IMaster<TgStopMap> config = new TgStopMapMaster();
        return new EntityMaster<TgStopMap>(
                TgStopMap.class,
                config,
                injector);
    }
}
