package ua.com.fielden.platform.web.resources.webui;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;

import java.util.Optional;

import ua.com.fielden.platform.sample.domain.MiUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserProducer;
import ua.com.fielden.platform.security.user.UserRolesUpdater;
import ua.com.fielden.platform.security.user.UserRolesUpdaterProducer;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

import com.google.inject.Injector;

/**
 * {@link User} Web UI configuration.
 *
 * @author TG Team
 *
 */
public class UserWebUiConfig {
    public final EntityMaster<UserRolesUpdater> userRolesUpdaterMaster;
    public final EntityCentre<User> userCentre;
    public final EntityMaster<User> userMaster;

    public UserWebUiConfig(final Injector injector) {
        userCentre = createUserCentre(injector);
        userMaster = createUserMaster(injector);
        userRolesUpdaterMaster = createUserRolesUpdaterMaster(injector);
    }

    /**
     * Creates entity centre for {@link User}.
     *
     * @return
     */
    public static EntityCentre<User> createUserCentre(final Injector injector) {
        final EntityCentre<User> userCentre = new EntityCentre<>(MiUser.class, "Users",
                EntityCentreBuilder.centreFor(User.class)
                .runAutomatically()
                .addCrit("this").asMulti().autocompleter(User.class).also()
                .addCrit("base").asMulti().bool().also()
                .addCrit("basedOnUser").asMulti().autocompleter(User.class)
                .setLayoutFor(Device.DESKTOP, Optional.empty(), "[['center-justified', 'start', ['margin-right: 40px', 'flex'], ['flex'], ['flex']]]")

                .addProp("this").also()
                .addProp("base").also()
                .addProp("basedOnUser")
                .addPrimaryAction(EntityActionConfig.createMasterInDialogInvocationActionConfig())
                .also()
                .addSecondaryAction(
                    action(UserRolesUpdater.class)
                    .withContext(context().withCurrentEntity().build())
                    .icon("add-circle")
                    .shortDesc("Add / Remove roles")
                    .longDesc("Add / Remove roles for this user")
                                        //.prefDimForView(mkDim(600, 750))
                    .build())
                .build(), injector, (centre) -> {
                    // ... please implement some additional hooks if necessary -- for e.g. centre.getFirstTick().setWidth(...), add calculated properties through domain tree API, etc.
                    centre.getSecondTick().setWidth(User.class, "", 60);
                    centre.getSecondTick().setWidth(User.class, "base", 60);
                    centre.getSecondTick().setWidth(User.class, "basedOnUser", 60);
                    return centre;
                });
        return userCentre;
    }

    /**
     * Creates entity master for {@link User}.
     *
     * @return
     */
    public static EntityMaster<User> createUserMaster(final Injector injector) {
        final String fmr = "'flex', 'margin-right: 20px'";
        final String actionMr = "'margin-top: 20px', 'margin-left: 20px', 'width: 110px'";

        final IMaster<User> masterConfigForUser = new SimpleMasterBuilder<User>()
                .forEntity(User.class)
                .addProp("key").asSinglelineText()
                .also()
                .addProp("base").asCheckbox()
                .also()
                .addProp("basedOnUser").asAutocompleter()
                .also()
                .addProp("roles").asCollectionalRepresentor()
                    .withAction(
                        action(UserRolesUpdater.class)
                        .withContext(context().withMasterEntity().build())
                        .icon("add-circle")
                        .shortDesc("Add / Remove roles")
                        .longDesc("Add / Remove roles")
                        .build())
                .also()
                .addAction(MasterActions.REFRESH)
                //      */.icon("trending-up") SHORT-CUT
                /*      */.shortDesc("CANCEL")
                /*      */.longDesc("Cancel action")
                .addAction(MasterActions.VALIDATE)
                .addAction(MasterActions.SAVE)
                .addAction(MasterActions.EDIT)
                .addAction(MasterActions.VIEW)

                .setLayoutFor(Device.DESKTOP, Optional.empty(), (
                        "      ['padding:20px', "
                        + format("[[%s], [%s], [%s], ['flex']],", fmr, fmr, fmr)
                        + format("['margin-top: 20px', 'wrap', [%s],[%s],[%s],[%s],[%s]]", actionMr, actionMr, actionMr, actionMr, actionMr)
                        + "    ]"))
                .done();
        return new EntityMaster<User>(
                User.class,
                UserProducer.class,
                masterConfigForUser,
                injector);
    }

    /**
     * Creates entity master for {@link UserRolesUpdater}.
     *
     * @return
     */
    public static EntityMaster<UserRolesUpdater> createUserRolesUpdaterMaster(final Injector injector) {
        final String actionMr = "'margin-top: 20px', 'margin-left: 20px', 'width: 110px'";
        final IMaster<UserRolesUpdater> masterConfig = new SimpleMasterBuilder<UserRolesUpdater>()
                .forEntity(UserRolesUpdater.class)
                .addProp("roles").asCollectionalEditor().maxVisibleRows(5)
                .also()
                .addAction(MasterActions.REFRESH)
                //      */.icon("trending-up") SHORT-CUT
                /*      */.shortDesc("CANCEL")
                /*      */.longDesc("Cancel action")
                .addAction(MasterActions.SAVE)

                .setLayoutFor(Device.DESKTOP, Optional.empty(), (
                        "      ['padding:20px', 'width:750px', "
                        + format("['flex', ['flex']],")
                        + format("['margin-top: 20px', 'wrap', [%s],[%s]]", actionMr, actionMr)
                        + "    ]"))
                .done();
        return new EntityMaster<UserRolesUpdater>(
                UserRolesUpdater.class,
                UserRolesUpdaterProducer.class,
                masterConfig,
                injector);
    }
}
