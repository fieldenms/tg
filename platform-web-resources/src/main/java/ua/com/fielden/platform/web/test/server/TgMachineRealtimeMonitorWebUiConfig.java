package ua.com.fielden.platform.web.test.server;

import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.sample.domain.TgMessage;
import ua.com.fielden.platform.sample.domain.TgMessageMap;
import ua.com.fielden.platform.sample.domain.TgOrgUnit;
import ua.com.fielden.platform.ui.menu.sample.MiTgMachineRealtimeMonitor;
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
 * {@link TgMachine} Web UI configuration.
 * 
 * @author Developers
 *
 */
public class TgMachineRealtimeMonitorWebUiConfig {

    public final EntityCentre<TgMachine> centre;

    public static TgMachineRealtimeMonitorWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new TgMachineRealtimeMonitorWebUiConfig(injector, builder);
    }

    private TgMachineRealtimeMonitorWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        centre = createCentre(injector);
        builder.register(centre);
        
//        builder.register(createTgMachineRealtimeMonitorMapMaster(injector));
    }

    /**
     * Creates entity centre for {@link TgMachine}.
     *
     * @param injector
     * @return created entity centre
     */
    private EntityCentre<TgMachine> createCentre(final Injector injector) {
        final EntityCentreConfig<TgMachine> centre = EntityCentreBuilder.centreFor(TgMachine.class)
                .runAutomatically()
                .addCrit("this").asMulti().autocompleter(TgMachine.class).also()
                .addCrit("orgUnit").asMulti().autocompleter(TgOrgUnit.class)
                .setLayoutFor(Device.DESKTOP, Optional.empty(), "[['center-justified', 'start', ['margin-right: 40px', 'flex'], ['flex']]]")
                .withScrollingConfig(ScrollConfig.configScroll().withFixedHeader().withFixedSummary().done())
                .setPageCapacity(10000)
                .setVisibleRowsCount(10)

                .addProp("this")
                    // .order(1).asc()
                    .width(80)
                    .withSummary("_countOfMachine", "COUNT(SELF)", "К-сть машин:Кількість вибраних машин.")
                .also()
                .addProp("orgUnit")
                    // .order(2).asc()
                    .width(150)
                    .withSummary("_countOfOrgUnit", "COUNT(orgUnit)", "К-сть підрозділів:Кількість унікальних підрозділів для вибраних машин.")
                .also()
                .addProp("lastMessage.gpsTime")
                    .width(150)
                .also()
                .addProp("lastMessage.vectorSpeed")
                    .width(110)
                // TODO .setRenderingCustomiser(TgMessageRenderingCustomiser.class)
                // TODO .setFetchProvider(EntityUtils.fetch(TgMessage.class).with("x", "y", "altitude", "vectorAngle"))
//                .addInsertionPoint(
//                    action(TgMachineRealtimeMonitorMap.class)
//                            .withContext(context().withSelectionCrit().build())
//                            .icon("credit-card")
//                            .shortDesc("TgMachineRealtimeMonitor map")
//                            .withNoParentCentreRefresh()
//                            .build(),
//                    InsertionPoints.BOTTOM
//                )
                .build();

        final EntityCentre<TgMachine> entityCentre = new EntityCentre<>(MiTgMachineRealtimeMonitor.class, "MiTgMachineRealtimeMonitor", centre, injector, null);
        return entityCentre;
    }
    
//    public static EntityMaster<TgMachineRealtimeMonitorMap> createTgMachineRealtimeMonitorMapMaster(final Injector injector) {
//        final IMaster<TgMachineRealtimeMonitorMap> config = new TgMachineRealtimeMonitorMapMaster();
//        return new EntityMaster<TgMachineRealtimeMonitorMap>(
//                TgMachineRealtimeMonitorMap.class,
//                config,
//                injector);
//    }
}
