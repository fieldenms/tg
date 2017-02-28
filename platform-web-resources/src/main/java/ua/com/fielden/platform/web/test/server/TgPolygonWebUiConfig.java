package ua.com.fielden.platform.web.test.server;

import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.sample.domain.TgPolygon;
import ua.com.fielden.platform.sample.domain.TgPolygonMap;
import ua.com.fielden.platform.ui.menu.sample.MiTgPolygon;
import ua.com.fielden.platform.utils.EntityUtils;
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
 * {@link TgPolygon} Web UI configuration.
 * 
 * @author Developers
 *
 */
public class TgPolygonWebUiConfig {

    public final EntityCentre<TgPolygon> centre;

    public static TgPolygonWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new TgPolygonWebUiConfig(injector, builder);
    }

    private TgPolygonWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        centre = createCentre(injector);
        builder.register(centre);
        
        builder.register(createTgPolygonMapMaster(injector));
    }

    /**
     * Creates entity centre for {@link TgPolygon}.
     *
     * @param injector
     * @return created entity centre
     */
    private EntityCentre<TgPolygon> createCentre(final Injector injector) {
        final EntityCentreConfig<TgPolygon> centre = EntityCentreBuilder.centreFor(TgPolygon.class)
                .runAutomatically()
                .addCrit("this").asMulti().autocompleter(TgPolygon.class).also()
                .addCrit("desc").asMulti().text()
                .setLayoutFor(Device.DESKTOP, Optional.empty(), "[['center-justified', 'start', ['margin-right: 40px', 'flex'], ['flex']]]")
                .withScrollingConfig(ScrollConfig.configScroll().withFixedHeader().withFixedSummary().done())
                .setPageCapacity(10000)
                .setVisibleRowsCount(10)

                .addProp("this")
                    .order(1).asc()
                    .width(200)
                    // .withSummary("_countOfAll", "COUNT(SELF)", "Кількість:Кількість гео-зон.")
                .also()
                .addProp("desc")
                    .order(2).asc()
                    .width(400)
                // .setRenderingCustomiser(TgMessageRenderingCustomiser.class)
                .setFetchProvider(EntityUtils.fetch(TgPolygon.class).with("coordinates.longitude", "coordinates.latitude"))
                .addInsertionPoint(
                    action(TgPolygonMap.class)
                            .withContext(context().withSelectionCrit().build())
                            .icon("credit-card")
                            .shortDesc("TgPolygon map")
                            .prefDimForView(mkDim("'auto'", "'400px'"))
                            .withNoParentCentreRefresh()
                            .build(),
                    InsertionPoints.BOTTOM
                )
                .build();

        final EntityCentre<TgPolygon> entityCentre = new EntityCentre<>(MiTgPolygon.class, "MiTgPolygon", centre, injector, null);
        return entityCentre;
    }
    
    public static EntityMaster<TgPolygonMap> createTgPolygonMapMaster(final Injector injector) {
        final IMaster<TgPolygonMap> config = new TgPolygonMapMaster();
        return new EntityMaster<TgPolygonMap>(
                TgPolygonMap.class,
                config,
                injector);
    }
}
