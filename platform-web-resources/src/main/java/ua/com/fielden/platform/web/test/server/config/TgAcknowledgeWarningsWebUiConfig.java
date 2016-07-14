package ua.com.fielden.platform.web.test.server.config;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.sample.domain.TgAcknowledgeWarnings;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;
/** 
 * {@link TgAcknowledgeWarnings} Web UI configuration.
 * 
 * @author Developers
 *
 */
public class TgAcknowledgeWarningsWebUiConfig {

    public final EntityMaster<TgAcknowledgeWarnings> master;

    public static TgAcknowledgeWarningsWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new TgAcknowledgeWarningsWebUiConfig(injector, builder);
    }

    private TgAcknowledgeWarningsWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        master = createMaster(injector);
        builder.register(master);
    }

    private EntityMaster<TgAcknowledgeWarnings> createMaster(final Injector injector) {
        final String layout = LayoutComposer.mkGridForMaster(640, 1, 1);

        final IMaster<TgAcknowledgeWarnings> masterConfig = new SimpleMasterBuilder<TgAcknowledgeWarnings>().forEntity(TgAcknowledgeWarnings.class)
                .addProp("acknowledged").asCheckbox().also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
                .addAction(MasterActions.SAVE)
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .done();

        return new EntityMaster<TgAcknowledgeWarnings>(TgAcknowledgeWarnings.class, masterConfig, injector);
    }
}