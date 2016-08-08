package ua.com.fielden.platform.web.test.server.config;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.sample.domain.AcknowledgeWarnings;
import ua.com.fielden.platform.sample.domain.AcknowledgeWarningsProducer;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
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
        final String layout = LayoutComposer.mkGridForMaster(640, 2, 1);

        final IMaster<AcknowledgeWarnings> masterConfig = new SimpleMasterBuilder<AcknowledgeWarnings>().forEntity(AcknowledgeWarnings.class)
                .addProp("acknowledged").asCheckbox().also()
                .addProp("allWarnings").asMultilineText().also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
                .addAction(MasterActions.SAVE)
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .done();

        return new EntityMaster<AcknowledgeWarnings>(AcknowledgeWarnings.class, AcknowledgeWarningsProducer.class, masterConfig, injector);
    }
}