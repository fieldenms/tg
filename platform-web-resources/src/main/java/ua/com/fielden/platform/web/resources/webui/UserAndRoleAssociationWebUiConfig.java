package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.web.PrefDim.mkDim;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.producers.UserAndRoleAssociationProducer;
import ua.com.fielden.platform.web.PrefDim.Unit;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.layout.api.impl.LayoutComposer;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

/**
 * {@link UserAndRoleAssociation} Web UI configuration.
 *
 * @author TG Team
 *
 */
public class UserAndRoleAssociationWebUiConfig {

    public final EntityMaster<UserAndRoleAssociation> master;

    public static UserAndRoleAssociationWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new UserAndRoleAssociationWebUiConfig(injector, builder);
    }

    private UserAndRoleAssociationWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        master = createMaster(injector);
        builder.register(master);
    }

    /**
     * Creates entity master for {@link UserAndRoleAssociation}.
     *
     * @param injector
     * @return created entity master
     */
    private EntityMaster<UserAndRoleAssociation> createMaster(final Injector injector) {
        final String layout = LayoutComposer.mkGridForMasterFitWidth(2, 1);

        final IMaster<UserAndRoleAssociation> masterConfig = new SimpleMasterBuilder<UserAndRoleAssociation>().forEntity(UserAndRoleAssociation.class)
                .addProp("user").asAutocompleter().also()
                .addProp("userRole").asAutocompleter().also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel changes if any and refresh.")
                .addAction(MasterActions.SAVE).shortDesc("Save").longDesc("Save changes.")
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), LayoutComposer.mkActionLayoutForMaster())
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .withDimensions(mkDim(360, 240, Unit.PX))
                .done();

        return new EntityMaster<>(UserAndRoleAssociation.class, UserAndRoleAssociationProducer.class, masterConfig, injector);
    }
}