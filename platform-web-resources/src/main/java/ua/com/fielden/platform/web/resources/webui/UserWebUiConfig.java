package ua.com.fielden.platform.web.resources.webui;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.centre.api.actions.ConfirmationPreAction.okCancel;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.EntityDeleteAction;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.entity.EntityNewAction;
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

/**
 * {@link User} Web UI configuration.
 *
 * @author TG Team
 *
 */
public class UserWebUiConfig {
    private static final String actionButton = "'margin: 10px', 'width: 110px'";
    private static final String bottomButtonPanel = "['horizontal', 'margin-top: 20px', 'justify-content: center', 'wrap', [%s], [%s]]";
    
    public final EntityMaster<UserRolesUpdater> rolesUpdater;
    public final EntityCentre<User> centre;
    public final EntityMaster<User> master;

    public UserWebUiConfig(final Injector injector) {
        centre = createCentre(injector);
        master = createMaster(injector);
        rolesUpdater = createRolesUpdater(injector);
    }

    /**
     * Creates entity centre for {@link User}.
     *
     * @return
     */
    private static EntityCentre<User> createCentre(final Injector injector) {
        final String critLayout = "['vertical', 'center-justified', "
                + "['flex'], "
                + "['flex'], "
                + "['flex']"
                + "]";
        
        final EntityCentre<User> userCentre = new EntityCentre<>(MiUser.class, "Users",
                EntityCentreBuilder.centreFor(User.class)
                .runAutomatically()
                .addTopAction(UserActions.NEW_ACTION.mkAction()).also()                
                .addTopAction(UserActions.DELETE_ACTION.mkAction())
                .addCrit("this").asMulti().autocompleter(User.class).also()
                .addCrit("base").asMulti().bool().also()
                .addCrit("basedOnUser").asMulti().autocompleter(User.class)
                .setLayoutFor(Device.DESKTOP, Optional.empty(), critLayout)
                .addProp("this")
                    .order(1).asc()
                    .width(200)
                .also()
                .addProp("basedOnUser").width(200).also()
                .addProp("base").width(80).also()
                .addProp("email").minWidth(150)
                .addPrimaryAction(UserActions.EDIT_ACTION.mkAction()).also()
                .addSecondaryAction(UserActions.MANAGE_ROLES_ACTION.mkAction())
                .build(), injector, null);
        return userCentre;
    }

    /**
     * Creates entity master for {@link User}.
     *
     * @return
     */
    private static EntityMaster<User> createMaster(final Injector injector) {
        final String fmr = "'flex', 'margin-right: 20px', 'width: 200px'";
        final String fmrLast = "'flex', 'width: 200px'";
        
        final String layout = 
            "['padding:20px', "
            + format("[[%s], [%s]], ", fmr, fmrLast)
            + format("[[%s], [%s]], ", fmr, fmrLast)
            + format(bottomButtonPanel, actionButton, actionButton)
            + "]";
        
        final IMaster<User> masterConfigForUser = new SimpleMasterBuilder<User>()
                .forEntity(User.class)
                .addProp("key").asSinglelineText()
                .also()
                .addProp("email").asSinglelineText()
                .also()                
                .addProp("base").asCheckbox()
                .also()
                .addProp("basedOnUser").asAutocompleter()
                .also()
                .addAction(MasterActions.REFRESH).shortDesc("CANCEL").longDesc("Cancel action")
                .addAction(MasterActions.SAVE)
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
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
    private static EntityMaster<UserRolesUpdater> createRolesUpdater(final Injector injector) {
        final IMaster<UserRolesUpdater> masterConfig = new SimpleMasterBuilder<UserRolesUpdater>()
                .forEntity(UserRolesUpdater.class)
                .addProp("roles").asCollectionalEditor().maxVisibleRows(5)
                .also()
                .addAction(MasterActions.REFRESH).shortDesc("CANCEL").longDesc("Cancel action")
                .addAction(MasterActions.SAVE)

                .setLayoutFor(Device.DESKTOP, Optional.empty(), (
                        "      ['padding:20px', 'width:750px', "
                        + format("['flex', ['flex']],")
                        + format(bottomButtonPanel, actionButton, actionButton)
                        + "    ]"))
                .done();
        return new EntityMaster<UserRolesUpdater>(
                UserRolesUpdater.class,
                UserRolesUpdaterProducer.class,
                masterConfig,
                injector);
    }
    
    private static enum UserActions {
        
        NEW_ACTION {
            @Override
            public EntityActionConfig mkAction() {
                return action(EntityNewAction.class)
                        .withContext(context().withSelectionCrit().build())
                        .icon("add-circle-outline")
                        .shortDesc("Add new User")
                        .longDesc("Initiates creation of a new User.")
                        .build();
            }

        },
        
        EDIT_ACTION {
            @Override
            public EntityActionConfig mkAction() {
                return action(EntityEditAction.class)
                        .withContext(context().withCurrentEntity().withSelectionCrit().build())
                        .icon("editor:mode-edit")
                        .shortDesc("Edit User")
                        .longDesc("Opens master for User editing.")
                        .build();
            }

        },
        
        DELETE_ACTION {
            @Override
            public EntityActionConfig mkAction() {
                final String desc = "Delete selected user(s).";
                return action(EntityDeleteAction.class)
                        .withContext(context().withSelectedEntities().build())
                        .preAction(okCancel("Please confirm whether the selected user(s) should be deleted?"))
                        .icon("remove-circle-outline")
                        .shortDesc(desc)
                        .longDesc(desc)
                        .build();
            }

        },
        
        MANAGE_ROLES_ACTION {
            @Override
            public EntityActionConfig mkAction() {
                return action(UserRolesUpdater.class)
                        .withContext(context().withCurrentEntity().build())
                        .icon("av:recent-actors")
                        .shortDesc("Add/Remove Roles")
                        .longDesc("Add/remove user roles.")
                        .build();
            }
            
        };

        public abstract EntityActionConfig mkAction();
    }
}
