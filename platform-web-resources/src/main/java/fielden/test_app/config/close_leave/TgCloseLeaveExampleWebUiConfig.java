package fielden.test_app.config.close_leave;

import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.action.CentreConfigurationWebUiConfig.CentreConfigActions.CUSTOMISE_COLUMNS_ACTION;
import static ua.com.fielden.platform.web.test.server.config.StandardActions.DELETE_ACTION;
import static ua.com.fielden.platform.web.test.server.config.StandardActions.EDIT_ACTION;
import static ua.com.fielden.platform.web.test.server.config.StandardActions.EXPORT_ACTION;
import static ua.com.fielden.platform.web.test.server.config.StandardActions.NEW_ACTION;
import static ua.com.fielden.platform.web.view.master.api.compound.Compound.openEdit;
import static ua.com.fielden.platform.web.view.master.api.compound.Compound.openNew;

import java.util.Optional;

import com.google.inject.Injector;

import fielden.test_app.close_leave.OpenTgCloseLeaveExampleMasterAction;
import fielden.test_app.close_leave.TgCloseLeaveExample;
import fielden.test_app.close_leave.TgCloseLeaveExampleDetail;
import fielden.test_app.close_leave.TgCloseLeaveExampleDetailUnpersisted;
import fielden.test_app.close_leave.TgCloseLeaveExampleMaster_OpenDetailUnpersisted_MenuItem;
import fielden.test_app.close_leave.TgCloseLeaveExampleMaster_OpenDetail_MenuItem;
import fielden.test_app.close_leave.TgCloseLeaveExampleMaster_OpenMain_MenuItem;
import fielden.test_app.close_leave.producers.OpenTgCloseLeaveExampleMasterActionProducer;
import fielden.test_app.close_leave.producers.TgCloseLeaveExampleDetailProducer;
import fielden.test_app.close_leave.producers.TgCloseLeaveExampleDetailUnpersistedProducer;
import fielden.test_app.main.menu.close_leave.MiTgCloseLeaveExample;
import ua.com.fielden.platform.web.PrefDim;
import ua.com.fielden.platform.web.PrefDim.Unit;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.layout.api.impl.LayoutComposer;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.compound.impl.CompoundMasterBuilder;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

/** 
 * {@link TgCloseLeaveExample} Web UI configuration.
 * 
 * @author TG Team
 *
 */
public class TgCloseLeaveExampleWebUiConfig {
    private final PrefDim prefDim = mkDim(1280, 600, Unit.PX);
    public final EntityCentre<TgCloseLeaveExample> centre;
    public final EntityMaster<TgCloseLeaveExample> master;
    
    public static TgCloseLeaveExampleWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new TgCloseLeaveExampleWebUiConfig(injector, builder);
    }
    
    private TgCloseLeaveExampleWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        centre = createCentre(injector);
        builder.register(centre);
        master = createMaster(injector);
        builder.register(master);
        
        final EntityMaster<OpenTgCloseLeaveExampleMasterAction> compoundMaster = CompoundMasterBuilder.<TgCloseLeaveExample, OpenTgCloseLeaveExampleMasterAction>create(injector, builder)
            .forEntity(OpenTgCloseLeaveExampleMasterAction.class)
            .withProducer(OpenTgCloseLeaveExampleMasterActionProducer.class)
            .addMenuItem(TgCloseLeaveExampleMaster_OpenMain_MenuItem.class)
                .icon("icons:picture-in-picture")
                .shortDesc("Main")
                .longDesc("Close Leave main")
                .withView(master)
            .also()
            .addMenuItem(TgCloseLeaveExampleMaster_OpenDetail_MenuItem.class)
                .icon("icons:picture-in-picture")
                .shortDesc("Detail")
                .longDesc("Close Leave detail")
                .withView(createDetailMaster(injector))
            .also()
            .addMenuItem(TgCloseLeaveExampleMaster_OpenDetailUnpersisted_MenuItem.class)
                .icon("icons:picture-in-picture")
                .shortDesc("Detail Unpersisted")
                .longDesc("Close Leave detail unpersisted")
                .withView(createDetailUnpersistedMaster(injector))
            .done();
        builder.register(compoundMaster);
    }
    
    private EntityCentre<TgCloseLeaveExample> createCentre(final Injector injector) {
        final String layout = LayoutComposer.mkGridForCentre(1, 2);
        final EntityCentreConfig<TgCloseLeaveExample> ecc = EntityCentreBuilder.centreFor(TgCloseLeaveExample.class)
            .addTopAction(NEW_ACTION.mkAction(TgCloseLeaveExample.class)).also()
            .addTopAction(openNew(OpenTgCloseLeaveExampleMasterAction.class, "add-circle-outline", "TgCloseLeaveExample", "Add new TgCloseLeaveExample", prefDim)).also()
            .addTopAction(DELETE_ACTION.mkAction(TgCloseLeaveExample.class)).also()
            .addTopAction(CUSTOMISE_COLUMNS_ACTION.mkAction()).also()
            .addTopAction(EXPORT_ACTION.mkAction(TgCloseLeaveExample.class))
            .addCrit("this").asMulti().autocompleter(TgCloseLeaveExample.class).also()
            .addCrit("desc").asMulti().text()
            .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
            .setLayoutFor(Device.TABLET, Optional.empty(), layout)
            .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
            .addProp("this").order(1).asc().width(100)
                .withSummary("total_count_", "COUNT(SELF)", "Count:The total number of matching TgCloseLeaveExample.")
                .withAction(openEdit(OpenTgCloseLeaveExampleMasterAction.class, "TgCloseLeaveExample", "Edit TgCloseLeaveExample", prefDim)).also()
            .addProp("desc").minWidth(400)
            .addPrimaryAction(EDIT_ACTION.mkAction(TgCloseLeaveExample.class))
        .build();
        return new EntityCentre<>(MiTgCloseLeaveExample.class, "MiTgCloseLeaveExample", ecc, injector, null);
    }
    
    private EntityMaster<TgCloseLeaveExample> createMaster(final Injector injector) {
        final String layout = LayoutComposer.mkGridForMaster(640, 2, 1);
        final IMaster<TgCloseLeaveExample> masterConfig = new SimpleMasterBuilder<TgCloseLeaveExample>().forEntity(TgCloseLeaveExample.class)
            .addProp("key").asSinglelineText().also()
            .addProp("desc").asMultilineText().also()
            .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
            .addAction(MasterActions.SAVE)
            .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), LayoutComposer.mkActionLayoutForMaster())
            .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
            .setLayoutFor(Device.TABLET, Optional.empty(), layout)
            .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
        .done();
        return new EntityMaster<>(TgCloseLeaveExample.class, masterConfig, injector);
    }
    
    private EntityMaster<TgCloseLeaveExampleDetail> createDetailMaster(final Injector injector) {
        final String layout = LayoutComposer.mkGridForMaster(640, 1, 1);
        final IMaster<TgCloseLeaveExampleDetail> masterConfig = new SimpleMasterBuilder<TgCloseLeaveExampleDetail>().forEntity(TgCloseLeaveExampleDetail.class)
            .addProp("desc").asMultilineText().also()
            .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
            .addAction(MasterActions.SAVE)
            .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), LayoutComposer.mkActionLayoutForMaster())
            .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
            .setLayoutFor(Device.TABLET, Optional.empty(), layout)
            .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
        .done();
        return new EntityMaster<TgCloseLeaveExampleDetail>(TgCloseLeaveExampleDetail.class, TgCloseLeaveExampleDetailProducer.class, masterConfig, injector);
    }
    
    private EntityMaster<TgCloseLeaveExampleDetailUnpersisted> createDetailUnpersistedMaster(final Injector injector) {
        final String layout = LayoutComposer.mkGridForMaster(640, 1, 1);
        final IMaster<TgCloseLeaveExampleDetailUnpersisted> masterConfig = new SimpleMasterBuilder<TgCloseLeaveExampleDetailUnpersisted>().forEntity(TgCloseLeaveExampleDetailUnpersisted.class)
            .addProp("desc").asMultilineText().also()
            .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
            .addAction(MasterActions.SAVE)
            .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), LayoutComposer.mkActionLayoutForMaster())
            .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
            .setLayoutFor(Device.TABLET, Optional.empty(), layout)
            .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
        .done();
        return new EntityMaster<TgCloseLeaveExampleDetailUnpersisted>(TgCloseLeaveExampleDetailUnpersisted.class, TgCloseLeaveExampleDetailUnpersistedProducer.class, masterConfig, injector);
    }
    
}