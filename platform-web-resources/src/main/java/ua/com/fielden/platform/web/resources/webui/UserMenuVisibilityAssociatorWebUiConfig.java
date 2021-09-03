package ua.com.fielden.platform.web.resources.webui;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.action.StandardMastersWebUiConfig.MASTER_ACTION_SPECIFICATION;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.menu.UserMenuVisibilityAssociator;
import ua.com.fielden.platform.menu.UserMenuVisibilityAssociatorProducer;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

public class UserMenuVisibilityAssociatorWebUiConfig {

    private static final String actionButton = MASTER_ACTION_SPECIFICATION;
    private static final String bottomButtonPanel = "['horizontal', 'padding: 20px', 'justify-content: center', 'wrap', [%s], [%s]]";

    public final EntityMaster<UserMenuVisibilityAssociator> master;

    public UserMenuVisibilityAssociatorWebUiConfig(final Injector injector) {
        master = createMaster(injector);
    }

    private EntityMaster<UserMenuVisibilityAssociator> createMaster(final Injector injector) {
        final IMaster<UserMenuVisibilityAssociator> masterConfig = new SimpleMasterBuilder<UserMenuVisibilityAssociator>()
                .forEntity(UserMenuVisibilityAssociator.class)
                .addProp("users").asCollectionalEditor()
                .also()
                .addAction(MasterActions.REFRESH).shortDesc("CANCEL").longDesc("Cancel action")
                .addAction(MasterActions.SAVE)

                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), format(bottomButtonPanel, actionButton, actionButton))
                .setLayoutFor(Device.DESKTOP, Optional.empty(), (
                        "      ['padding:20px', 'height: 100%', 'box-sizing: border-box', "
                        + format("['flex', ['flex']]")
                        + "    ]"))
                .withDimensions(mkDim("'30%'", "'50%'"))
                .done();
        return new EntityMaster<UserMenuVisibilityAssociator>(
                UserMenuVisibilityAssociator.class,
                UserMenuVisibilityAssociatorProducer.class,
                masterConfig,
                injector);
    }
}
