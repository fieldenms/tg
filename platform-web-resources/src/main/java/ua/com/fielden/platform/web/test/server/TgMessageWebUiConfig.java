package ua.com.fielden.platform.web.test.server;

import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.sample.domain.TgMessage;
import ua.com.fielden.platform.sample.domain.TgMessageMap;
import ua.com.fielden.platform.ui.menu.sample.MiTgMessage;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
/** 
 * {@link Eq} Web UI configuration.
 * 
 * @author Developers
 *
 */
public class TgMessageWebUiConfig {

    public final EntityCentre<TgMessage> centre;

    public static TgMessageWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new TgMessageWebUiConfig(injector, builder);
    }

    private TgMessageWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        centre = createCentre(injector);
        builder.register(centre);
        
        builder.register(createTgMessageMapMaster(injector));
    }

    /**
     * Creates entity centre for {@link TgMessage}.
     *
     * @param injector
     * @return created entity centre
     */
    private EntityCentre<TgMessage> createCentre(final Injector injector) {
        final EntityCentreConfig<TgMessage> centre = EntityCentreBuilder.centreFor(TgMessage.class)
                .runAutomatically()
                .addCrit("machine").asMulti().autocompleter(TgMachine.class).also()
                .addCrit("gpsTime").asRange().date()
                .setLayoutFor(Device.DESKTOP, Optional.empty(), "[['center-justified', 'start', ['margin-right: 40px', 'flex'], ['flex']]]")

                .addProp("machine")
                    .order(1).asc()
                    .width(90)
                .also()
                .addProp("gpsTime")
                    .order(2).asc()
                    .width(160)
                    .withSummary("_countOfAll", "COUNT(SELF)", "Кількість:Кількість повідомлень від модуля.")
                .also()
                .addProp("vectorSpeed")
                    .width(100)
                .also()
                .addProp("travelledDistance")
                    .width(100)
                    .withSummary("_sumOfTravelledDistance", "SUM(travelledDistance) / 1000.0", "Відстань (км):Сумарна відстань у кілометрах, пройдена вибраною машиною за вибраний час.")
                .also()
                .addProp("din1")
                    .width(90)
                .setFetchProvider(EntityUtils.fetch(TgMessage.class).with("x", "y", "altitude", "vectorAngle"))
                .addInsertionPoint(
                    action(TgMessageMap.class)
                            .withContext(context().withSelectionCrit().build())
                            .icon("credit-card")
                            .shortDesc("TgMessage map")
                            .withNoParentCentreRefresh()
                            .build(),
                    InsertionPoints.BOTTOM
                )
                .build();

        final EntityCentre<TgMessage> entityCentre = new EntityCentre<>(MiTgMessage.class, "MiTgMessage", centre, injector, null);
        return entityCentre;
    }
    
    public static EntityMaster<TgMessageMap> createTgMessageMapMaster(final Injector injector) {
        final IMaster<TgMessageMap> config = new TgMessageMapMaster();
        return new EntityMaster<TgMessageMap>(
                TgMessageMap.class,
                config,
                injector);
    }
}
