package ua.com.fielden.platform.web.resources.webui;

import static java.util.Optional.empty;
import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.mkActionLayoutForMaster;

import com.google.inject.Injector;

import ua.com.fielden.platform.menu.UserMenuVisibilityAssociator;
import ua.com.fielden.platform.menu.UserMenuVisibilityAssociatorProducer;
import ua.com.fielden.platform.web.PrefDim.Unit;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

/**
 * Web UI configuration for {@link UserMenuVisibilityAssociator}.
 * 
 * @author TG Team
 *
 */
public class UserMenuVisibilityAssociatorWebUiConfig {

    public final EntityMaster<UserMenuVisibilityAssociator> master;

    public UserMenuVisibilityAssociatorWebUiConfig(final Injector injector) {
        master = createMaster(injector);
    }

    private EntityMaster<UserMenuVisibilityAssociator> createMaster(final Injector injector) {
        final IMaster<UserMenuVisibilityAssociator> masterConfig = new SimpleMasterBuilder<UserMenuVisibilityAssociator>()
                .forEntity(UserMenuVisibilityAssociator.class)
                .addProp("users").asCollectionalEditor().withStaticOrder().also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel changes, if any, and close the dialog.")
                .addAction(MasterActions.SAVE).shortDesc("Save").longDesc("Save changes.")
                .setActionBarLayoutFor(Device.DESKTOP, empty(), mkActionLayoutForMaster())
                .setLayoutFor(Device.DESKTOP, empty(), "['padding:20px', 'height: 100%', 'box-sizing: border-box', ['flex', ['flex']] ]")
                .withDimensions(mkDim(30, 75, Unit.PRC))
                .done();
        return new EntityMaster<>(UserMenuVisibilityAssociator.class, UserMenuVisibilityAssociatorProducer.class, masterConfig, injector);
    }

}