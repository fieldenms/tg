package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.web.interfaces.ILayout.Device.DESKTOP;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.MOBILE;
import static ua.com.fielden.platform.web.interfaces.ILayout.Device.TABLET;
import static ua.com.fielden.platform.web.test.server.config.LayoutComposer.mkActionLayoutForMaster;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.functional.master.AcknowledgeWarnings;
import ua.com.fielden.platform.entity.functional.master.AcknowledgeWarningsProducer;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.test.server.config.LayoutComposer;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;
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
        final String layout = LayoutComposer.mkGridForEmbeddedMaster(1, 1);

        final IMaster<AcknowledgeWarnings> masterConfig = new SimpleMasterBuilder<AcknowledgeWarnings>().forEntity(AcknowledgeWarnings.class)
                .addProp("warnings").asCollectionalEditor().also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
                .addAction(MasterActions.SAVE).shortDesc("Accept").longDesc("Acknowledge warnings and continue saving")
                .setActionBarLayoutFor(DESKTOP, Optional.empty(), mkActionLayoutForMaster())
                .setActionBarLayoutFor(TABLET, Optional.empty(), mkActionLayoutForMaster())
                .setActionBarLayoutFor(MOBILE, Optional.empty(), mkActionLayoutForMaster())
                .setLayoutFor(DESKTOP, Optional.empty(), layout)
                .setLayoutFor(TABLET, Optional.empty(), layout)
                .setLayoutFor(MOBILE, Optional.empty(), layout)
                .done();

        return new EntityMaster<AcknowledgeWarnings>(AcknowledgeWarnings.class, AcknowledgeWarningsProducer.class, masterConfig, injector);
    }
}