package ua.com.fielden.platform.web.resources.webui;

import static java.util.Optional.empty;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.security.user.User.EMAIL;
import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.action.pre.ConfirmationPreAction.okCancel;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.mkActionLayoutForMaster;
import static ua.com.fielden.platform.web.test.server.config.LocatorFactory.mkLocator;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.EntityDeleteAction;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.security.user.ReUser;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.security.user.UserRolesUpdater;
import ua.com.fielden.platform.security.user.UserRolesUpdaterProducer;
import ua.com.fielden.platform.security.user.locator.UserLocator;
import ua.com.fielden.platform.security.user.value_matchers.UserMasterBaseUserMatcher;
import ua.com.fielden.platform.ui.menu.sample.MiUser;
import ua.com.fielden.platform.web.PrefDim.Unit;
import ua.com.fielden.platform.web.action.pre.EntityNavigationPreAction;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.layout.api.impl.LayoutComposer;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

/**
 * {@link User} Web UI configuration.
 *
 * @author TG Team
 *
 */
public class UserWebUiConfig {

    public final EntityMaster<UserRolesUpdater> rolesUpdater;
    public final EntityCentre<ReUser> centre;
    public final EntityMaster<User> master;

    public static UserWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new UserWebUiConfig(injector, builder);
    }

    private UserWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        centre = createCentre(injector, builder, mkLocator(builder, injector, UserLocator.class, "user"));
        master = createMaster(injector);
        rolesUpdater = createRolesUpdater(injector);
    }

    /**
     * Creates entity centre for {@link User}.
     *
     * @return
     */
    private static EntityCentre<ReUser> createCentre(final Injector injector, final IWebUiBuilder builder, final EntityActionConfig locator) {
        final String layout = LayoutComposer.mkVarGridForCentre(2, 2, 2);

        final EntityActionConfig userNewAction = UserActions.NEW_ACTION.mkAction();
        final EntityActionConfig userEditAction = UserActions.EDIT_ACTION.mkAction();
        builder.registerOpenMasterAction(User.class, userEditAction);

        final EntityCentre<ReUser> userCentre = new EntityCentre<>(MiUser.class,
                EntityCentreBuilder.centreFor(ReUser.class)
                .addFrontAction(userNewAction).also()
                .addFrontAction(locator)
                .addTopAction(userNewAction).also()
                .addTopAction(UserActions.DELETE_ACTION.mkAction()).also()
                .addTopAction(locator)
                .addCrit("this").asMulti().autocompleter(User.class).also()
                .addCrit("basedOnUser").asMulti().autocompleter(User.class).also()
                .addCrit(ACTIVE).asMulti().bool().also()
                .addCrit("base").asMulti().bool().also()
                .addCrit(EMAIL).asMulti().text().also()
                .addCrit("userRoles").asMulti().autocompleter(UserRole.class)
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .addProp("this")
                    .order(1).asc()
                    .width(200)
                    .withSummary("total_count_", "COUNT(SELF)", "Count:The total number of matching users.")
                .also()
                .addProp("basedOnUser").width(200).also()
                .addProp("base").width(80).also()
                .addProp(EMAIL).minWidth(150).also()
                .addProp(ACTIVE).width(50).also()
                .addProp("roles").minWidth(70).withAction(UserActions.MANAGE_ROLES_SECONDARY_ACTION.mkAction())
                .addPrimaryAction(userEditAction).also()
                .addSecondaryAction(UserActions.MANAGE_ROLES_SECONDARY_ACTION.mkAction())
                .build(), injector);
        return userCentre;
    }

    /**
     * Creates entity master for {@link User}.
     *
     * @return
     */
    private static EntityMaster<User> createMaster(final Injector injector) {
        final String layout = LayoutComposer.mkVarGridForMasterFitWidth(2, 2, 1, 1);

        final IMaster<User> masterConfigForUser = new SimpleMasterBuilder<User>()
                .forEntity(User.class)
                .addProp("key").asSinglelineText().also()
                .addProp("basedOnUser").asAutocompleter().withMatcher(UserMasterBaseUserMatcher.class).also()
                .addProp("active").asCheckbox().also()
                .addProp("base").asCheckbox().also()
                .addProp("email").asSinglelineText().also()
                .addProp("roles").asCollectionalRepresentor().withAction(UserActions.MANAGE_ROLES_MASTER_PROP_ACTION.mkAction()).also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel changes if any and refresh.")
                .addAction(MasterActions.SAVE).shortDesc("Save").longDesc("Save changes.")
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), mkActionLayoutForMaster())
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .withDimensions(mkDim(580, 390))
                .done();
        return new EntityMaster<>(User.class, masterConfigForUser, injector);
    }

    /**
     * Creates entity master for {@link UserRolesUpdater}.
     *
     * @return
     */
    private static EntityMaster<UserRolesUpdater> createRolesUpdater(final Injector injector) {
        final IMaster<UserRolesUpdater> masterConfig = new SimpleMasterBuilder<UserRolesUpdater>()
                .forEntity(UserRolesUpdater.class)
                .addProp("roles").asCollectionalEditor().also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel changes, if any, and close the dialog.")
                .addAction(MasterActions.SAVE).shortDesc("Save").longDesc("Save changes.")
                .setActionBarLayoutFor(Device.DESKTOP, empty(), mkActionLayoutForMaster())
                .setLayoutFor(Device.DESKTOP, empty(), "['padding:20px', 'height: 100%', 'box-sizing: border-box', ['flex', ['flex']] ]")
                .withDimensions(mkDim(30, 75, Unit.PRC))
                .done();
        return new EntityMaster<>(UserRolesUpdater.class, UserRolesUpdaterProducer.class, masterConfig, injector);
    }

    private static enum UserActions {

        NEW_ACTION {
            @Override
            public EntityActionConfig mkAction() {
                return action(EntityNewAction.class)
                        .withContext(context().withSelectionCrit().withComputation((entity, context) -> User.class).build())
                        .icon("add-circle-outline")
                        .shortDesc("Add new User")
                        .longDesc("Initiates creation of a new User.")
                        .shortcut("alt+n")
                        .build();
            }

        },

        EDIT_ACTION {
            @Override
            public EntityActionConfig mkAction() {
                return action(EntityEditAction.class)
                        .withContext(context().withCurrentEntity().withSelectionCrit().build())
                        .preAction(new EntityNavigationPreAction("User"))
                        .icon("editor:mode-edit")
                        .shortDesc("Edit User")
                        .longDesc("Opens master for User editing.")
                        .withNoParentCentreRefresh()
                        .build();
            }

        },

        DELETE_ACTION {
            @Override
            public EntityActionConfig mkAction() {
                final String desc = "Delete selected user(s).";
                return action(EntityDeleteAction.class)
                        .withContext(context().withSelectedEntities().withComputation((entity, context) -> User.class).build())
                        .preAction(okCancel("Please confirm whether the selected user(s) should be deleted?"))
                        .icon("remove-circle-outline")
                        .shortDesc(desc)
                        .longDesc(desc)
                        .shortcut("alt+d")
                        .build();
            }

        },

        MANAGE_ROLES_SECONDARY_ACTION {
            @Override
            public EntityActionConfig mkAction() {
                return action(UserRolesUpdater.class)
                        .withContext(context().withCurrentEntity().build())
                        .icon("av:recent-actors")
                        .shortDesc("Add/Remove Roles")
                        .longDesc("Add/remove user roles.")
                        .build();
            }

        },

        MANAGE_ROLES_MASTER_PROP_ACTION {
            @Override
            public EntityActionConfig mkAction() {
                return action(UserRolesUpdater.class)
                        .withContext(context().withMasterEntity().build())
                        .icon("av:recent-actors")
                        .shortDesc("Add/Remove Roles")
                        .longDesc("Add/remove user roles.")
                        .build();
            }

        };

        public abstract EntityActionConfig mkAction();
    }
}
