package ua.com.fielden.platform.web.resources.webui;

import java.util.Optional;

import ua.com.fielden.platform.entity.functional.master.AcknowledgeWarnings;
import ua.com.fielden.platform.entity.functional.master.AcknowledgeWarningsProducer;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.test.server.config.LayoutComposer;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

import com.google.inject.Injector;
/**
 * {@link AcknowledgeWarnings} Web UI configuration.
 *
 * @author Developers
 *
 */
public class AcknowledgeWarningsWebUiConfig {

    public final EntityMaster<AcknowledgeWarnings> master;

    public static AcknowledgeWarningsWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new AcknowledgeWarningsWebUiConfig(injector, builder);
    }

    private AcknowledgeWarningsWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        master = createMaster(injector);
        builder.register(master);
    }

    private EntityMaster<AcknowledgeWarnings> createMaster(final Injector injector) {
        final String layout = LayoutComposer.mkGridForMaster(640, 1, 1);

        final IMaster<AcknowledgeWarnings> masterConfig = new SimpleMasterBuilder<AcknowledgeWarnings>().forEntity(AcknowledgeWarnings.class)
                .addProp("warnings").asCollectionalEditor().also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
                .addAction(MasterActions.SAVE).shortDesc("Acknowledge").longDesc("Acknowledge warnings and continue saving")
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), LayoutComposer.mkActionLayoutForMaster())
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .done();

        return new EntityMaster<AcknowledgeWarnings>(AcknowledgeWarnings.class, AcknowledgeWarningsProducer.class, masterConfig, injector);
    }
}