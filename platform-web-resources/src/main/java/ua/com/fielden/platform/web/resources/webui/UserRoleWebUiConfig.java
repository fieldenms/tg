package ua.com.fielden.platform.web.resources.webui;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.sample.domain.MiUserRole;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.security.user.UserRoleProducer;
import ua.com.fielden.platform.security.user.UserRoleTokensUpdater;
import ua.com.fielden.platform.security.user.UserRoleTokensUpdaterProducer;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

/**
 * {@link UserRole} Web UI configuration.
 *
 * @author TG Team
 *
 */
public class UserRoleWebUiConfig {
    private static final String actionButton = "'margin: 10px', 'width: 110px'";
    private static final String bottomButtonPanel = "['horizontal', 'margin-top: 20px', 'justify-content: center', 'wrap', [%s], [%s]]";
    
    public final EntityMaster<UserRoleTokensUpdater> tokensUpdater;
    public final EntityCentre<UserRole> centre;
    public final EntityMaster<UserRole> master;

    public UserRoleWebUiConfig(final Injector injector) {
        centre = createCentre(injector);
        master = createMaster(injector);
        tokensUpdater = createTokensUpdater(injector);
    }

    /**
     * Creates entity centre for {@link UserRole}.
     *
     * @return
     */
    private static EntityCentre<UserRole> createCentre(final Injector injector) {
        final EntityCentre<UserRole> userRoleCentre = new EntityCentre<>(MiUserRole.class, "User Roles",
                EntityCentreBuilder.centreFor(UserRole.class)
                .runAutomatically()
                .addCrit("this").asMulti().autocompleter(UserRole.class).also()
                .addCrit("desc").asMulti().text()
                .setLayoutFor(Device.DESKTOP, Optional.empty(), "[['center-justified', 'start', ['margin-right: 40px', 'flex'], ['flex']]]")

                .addProp("this").also()
                .addProp("desc")
                .addPrimaryAction(EntityActionConfig.createMasterInDialogInvocationActionConfig())
                .also()
                .addSecondaryAction(
                    action(UserRoleTokensUpdater.class)
                    .withContext(context().withCurrentEntity().build())
                    .icon("add-circle")
                    .shortDesc("Add / Remove tokens")
                    .longDesc("Add / Remove tokens for this user role")
                                        //.prefDimForView(mkDim(600, 750))
                    .build())
                .build(), injector, (centre) -> {
                    // ... please implement some additional hooks if necessary -- for e.g. centre.getFirstTick().setWidth(...), add calculated properties through domain tree API, etc.
                    centre.getSecondTick().setWidth(UserRole.class, "", 120);
                    centre.getSecondTick().setWidth(UserRole.class, "desc", 360);
                    return centre;
                });
        return userRoleCentre;
    }

    /**
     * Creates entity master for {@link UserRole}.
     *
     * @return
     */
    private static EntityMaster<UserRole> createMaster(final Injector injector) {
        final String fmr = "'flex', 'margin-right: 20px'";

        final IMaster<UserRole> masterConfigForUserRole = new SimpleMasterBuilder<UserRole>()
                .forEntity(UserRole.class)
                .addProp("key").asSinglelineText()
                .also()
                .addProp("desc").asSinglelineText()
                .also()
                .addAction(MasterActions.REFRESH).shortDesc("CANCEL").longDesc("Cancel action")
                .addAction(MasterActions.SAVE)

                .setLayoutFor(Device.DESKTOP, Optional.empty(), (
                        "      ['padding:20px', "
                        + format("[[%s], ['flex']],", fmr)
                        + format(bottomButtonPanel, actionButton, actionButton)
                        + "    ]"))
                .done();
        return new EntityMaster<UserRole>(
                UserRole.class,
                UserRoleProducer.class,
                masterConfigForUserRole,
                injector);
    }

    /**
     * Creates entity master for {@link UserRoleTokensUpdater}.
     *
     * @return
     */
    private static EntityMaster<UserRoleTokensUpdater> createTokensUpdater(final Injector injector) {
        final IMaster<UserRoleTokensUpdater> masterConfig = new SimpleMasterBuilder<UserRoleTokensUpdater>()
                .forEntity(UserRoleTokensUpdater.class)
                .addProp("tokens").asCollectionalEditor().maxVisibleRows(5).withHeader("title")
                .also()
                .addAction(MasterActions.REFRESH).shortDesc("CANCEL").longDesc("Cancel action")
                .addAction(MasterActions.SAVE)

                .setLayoutFor(Device.DESKTOP, Optional.empty(), (
                        "      ['padding:20px', 'width:500px', "
                        + format("['flex', ['flex']],")
                        + format(bottomButtonPanel, actionButton, actionButton)
                        + "    ]"))
                .done();
        return new EntityMaster<UserRoleTokensUpdater>(
                UserRoleTokensUpdater.class,
                UserRoleTokensUpdaterProducer.class,
                masterConfig,
                injector);
    }
}
