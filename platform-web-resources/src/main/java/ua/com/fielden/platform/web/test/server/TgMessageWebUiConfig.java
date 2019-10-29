package ua.com.fielden.platform.web.test.server;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.construction.options.DefaultValueOptions.*;
import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.action.StandardMastersWebUiConfig.MASTER_ACTION_SPECIFICATION;
import java.util.Optional;

import org.joda.time.DateTime;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.sample.domain.TgMessage;
import ua.com.fielden.platform.sample.domain.TgMessageMap;
import ua.com.fielden.platform.sample.domain.TgMessageProducer;
import ua.com.fielden.platform.ui.menu.sample.MiTgMessage;
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
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;
/** 
 * {@link Eq} Web UI configuration.
 * 
 * @author Developers
 *
 */
public class TgMessageWebUiConfig {

    public final EntityCentre<TgMessage> centre;
    final EntityMaster<TgMessage> master; 

    public static TgMessageWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new TgMessageWebUiConfig(injector, builder);
    }

    private TgMessageWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        centre = createCentre(injector);
        builder.register(centre);
        
        builder.register(createTgMessageMapMaster(injector));

        final SimpleMasterBuilder<TgMessage> masterBuilder = new SimpleMasterBuilder<TgMessage>();
        final String actionStyle = MASTER_ACTION_SPECIFICATION;
        final String outer = "'flex', 'min-width:200px'";

        final String desktopTabletMasterLayout = ("['padding:20px',"
                + format("['justified', [%s]],", outer)
                + format("['justified', [%s]],", outer)
                + format("['justified', [%s]],", outer)
                + format("['justified', [%s]],", outer)
                + format("['justified', [%s]],", outer)
                + format("['justified', [%s]],", outer)
                + format("['justified', [%s]]", outer)
                + "]");
        final String actionBarLayout = format("['horizontal', 'padding: 20px', 'wrap', 'justify-content: center', [%s],   [%s]]", actionStyle, actionStyle);
        final IMaster<TgMessage> masterConfig = masterBuilder.forEntity(TgMessage.class)
                .addProp("machine").asAutocompleter().also()
                .addProp("gpsTime").asDateTimePicker().also()
                .addProp("travelledDistance").asDecimal().also()
                .addProp("vectorAngle").asSpinner().also()
                .addProp("vectorSpeed").asSpinner().also()
                .addProp("x").asDecimal().also()
                .addProp("y").asDecimal().also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancels current changes if any or refresh the data")
                .addAction(MasterActions.SAVE)
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), actionBarLayout)
                .setLayoutFor(Device.DESKTOP, Optional.empty(), desktopTabletMasterLayout).done();
        master = new EntityMaster<TgMessage>(TgMessage.class, TgMessageProducer.class, masterConfig, injector);
        builder.register(master);
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
                .addTopAction(action(EntityNewAction.class).
                        withContext(context().withSelectionCrit().build()).
                        icon("add-circle-outline").
                        shortDesc("Add new").
                        longDesc("Start coninuous creatio of entities").
                        shortcut("alt+n").
                        withNoParentCentreRefresh().
                        build())
                .addCrit("machine").asMulti().autocompleter(TgMachine.class).setDefaultValue(multi().string().setValues("07101ТА").value()).also()
                .addCrit("gpsTime").asRange().date().setDefaultValue(range().date().setFromValue(new DateTime(2000, 1, 1, 0, 0).toDate()).setToValue(new DateTime(2014, 5, 26, 23, 59).toDate()).value())
                .setLayoutFor(Device.DESKTOP, Optional.empty(), "[['center-justified', 'start', ['margin-right: 40px', 'flex'], ['flex']]]")
                .withScrollingConfig(ScrollConfig.configScroll().withFixedHeader().withFixedSummary().done())
                .setPageCapacity(10000)
                .setVisibleRowsCount(10)

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
                .setRenderingCustomiser(TgMessageRenderingCustomiser.class)
                .setFetchProvider(EntityUtils.fetch(TgMessage.class).with("x", "y", "altitude", "vectorAngle"))
                .addInsertionPoint(
                    action(TgMessageMap.class)
                            .withContext(context().withSelectionCrit().build())
                            .icon("credit-card")
                            .shortDesc("TgMessage map")
                            .prefDimForView(mkDim("'auto'", "'400px'"))
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
