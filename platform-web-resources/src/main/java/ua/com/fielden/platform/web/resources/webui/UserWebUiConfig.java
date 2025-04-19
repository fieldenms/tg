package ua.com.fielden.platform.web.resources.webui;

import com.google.inject.Injector;
import ua.com.fielden.platform.entity.EntityDeleteAction;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.security.user.*;
import ua.com.fielden.platform.security.user.locator.UserLocator;
import ua.com.fielden.platform.security.user.master.menu.actions.UserMaster_OpenMain_MenuItem;
import ua.com.fielden.platform.security.user.master.menu.actions.UserMaster_OpenUserAndRoleAssociation_MenuItem;
import ua.com.fielden.platform.security.user.ui_actions.OpenUserMasterAction;
import ua.com.fielden.platform.security.user.ui_actions.producers.OpenUserMasterActionProducer;
import ua.com.fielden.platform.security.user.value_matchers.UserMasterBaseUserMatcher;
import ua.com.fielden.platform.ui.menu.sample.MiUser;
import ua.com.fielden.platform.ui.menu.sample.MiUserMaster_UserAndRoleAssociation;
import ua.com.fielden.platform.web.PrefDim;
import ua.com.fielden.platform.web.PrefDim.Unit;
import ua.com.fielden.platform.web.action.CentreConfigurationWebUiConfig.CentreConfigActions;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.layout.api.impl.LayoutComposer;
import ua.com.fielden.platform.web.test.server.config.StandardActions;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.compound.Compound;
import ua.com.fielden.platform.web.view.master.api.compound.impl.CompoundMasterBuilder;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

import java.util.Optional;

import static java.util.Optional.empty;
import static ua.com.fielden.platform.dao.AbstractOpenCompoundMasterDao.enhanceEmbededCentreQuery;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.createConditionProperty;
import static ua.com.fielden.platform.security.user.User.EMAIL;
import static ua.com.fielden.platform.security.user.User.SSO_ONLY;
import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.action.pre.PreActions.entityNavigation;
import static ua.com.fielden.platform.web.action.pre.PreActions.okCancel;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.mkActionLayoutForMaster;
import static ua.com.fielden.platform.web.test.server.config.LocatorFactory.mkLocator;

/**
 * {@link User} Web UI configuration.
 *
 * @author TG Team
 *
 */
public class UserWebUiConfig {

    public static final String USER_TITLE = "User";

    public final EntityMaster<UserRolesUpdater> rolesUpdater;
    public final EntityCentre<ReUser> centre;
    public final EntityMaster<User> master;

    public static UserWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        final UserWebUiConfig userWebUiConfig = new UserWebUiConfig(injector, builder, false);
        UserAndRoleAssociationWebUiConfig.register(injector, builder);
        return userWebUiConfig;
    }

    public static UserWebUiConfig registerWithCompoundMaster(final Injector injector, final IWebUiBuilder builder) {
        final UserWebUiConfig userWebUiConfig = new UserWebUiConfig(injector, builder, true);
        UserAndRoleAssociationWebUiConfig.register(injector, builder);
        return userWebUiConfig;
    }

    private UserWebUiConfig(final Injector injector, final IWebUiBuilder builder, final boolean withCompoundMaster) {
        centre = createCentre(injector, builder, mkLocator(builder, injector, UserLocator.class, "user"), withCompoundMaster);
        master = createMaster(injector);
        rolesUpdater = createRolesUpdater(injector);

        if (withCompoundMaster) {
            CompoundMasterBuilder.<User, OpenUserMasterAction>create(injector, builder)
                    .forEntity(OpenUserMasterAction.class)
                    .withProducer(OpenUserMasterActionProducer.class)
                    .addMenuItem(UserMaster_OpenMain_MenuItem.class)
                        .icon("icons:picture-in-picture")
                        .shortDesc(OpenUserMasterAction.MAIN)
                        .longDesc("Application User" + " main")
                        .withView(master)
                    .also()
                    .addMenuItem(UserMaster_OpenUserAndRoleAssociation_MenuItem.class)
                        .icon("av:recent-actors")
                        .shortDesc(OpenUserMasterAction.ROLES)
                        .longDesc("Application User" + " " + OpenUserMasterAction.ROLES)
                        .withView(createUserAndRoleAssociationCentre(injector))
                    .done();
        }
    }

    /**
     * Creates entity centre for {@link User}.
     *
     * @return
     */
    private EntityCentre<ReUser> createCentre(final Injector injector, final IWebUiBuilder builder, final EntityActionConfig locator, final boolean withCompoundMaster) {
        final EntityActionConfig editUserAction;
        final EntityActionConfig newUserAction;
        // different edit and new actions are needed depending on whether compound or simpler master is in use
        if (withCompoundMaster) {
            final PrefDim dims = mkDim(960, 640, Unit.PX);
            editUserAction = Compound.openEdit(OpenUserMasterAction.class, USER_TITLE, "Edit " + USER_TITLE, dims);
            newUserAction = Compound.openNew(OpenUserMasterAction.class, "add-circle-outline", USER_TITLE, "Add " + USER_TITLE, dims);
            builder.registerOpenMasterAction(User.class, editUserAction);
        } else {
            newUserAction = UserActions.NEW_ACTION.mkAction();
            editUserAction = UserActions.EDIT_ACTION.mkAction();
            builder.registerOpenMasterAction(User.class, editUserAction);
        }

        final String layout = LayoutComposer.mkVarGridForCentre(2, 2, 2, 1);
        final EntityCentre<ReUser> userCentre = new EntityCentre<>(MiUser.class,
                EntityCentreBuilder.centreFor(ReUser.class)
                .addFrontAction(newUserAction).also()
                .addFrontAction(locator)
                .addTopAction(newUserAction).also()
                .addTopAction(UserActions.DELETE_ACTION.mkAction()).also()
                .addTopAction(locator).also()
                .addTopAction(CentreConfigActions.CUSTOMISE_COLUMNS_ACTION.mkAction()).also()
                .addTopAction(StandardActions.EXPORT_ACTION.mkAction(ReUser.class))
                .addCrit("this").asMulti().autocompleter(User.class).also()
                .addCrit("basedOnUser").asMulti().autocompleter(User.class).also()
                .addCrit(ACTIVE).asMulti().bool().also()
                .addCrit("base").asMulti().bool().also()
                .addCrit(EMAIL).asMulti().text().also()
                .addCrit(SSO_ONLY).asMulti().bool().also()
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
                .addProp(SSO_ONLY).width(50).also()
                .addProp("roles").minWidth(70).withAction(UserActions.MANAGE_ROLES_SECONDARY_ACTION.mkAction())
                .addPrimaryAction(editUserAction).also()
                .addSecondaryAction(UserActions.MANAGE_ROLES_SECONDARY_ACTION.mkAction())
                .build(), injector);
        return userCentre;
    }

    /**
     * Creates entity master for {@link User}.
     *
     * @return
     */
    private EntityMaster<User> createMaster(final Injector injector) {
        final String layout = LayoutComposer.mkVarGridForMasterFitWidth(2, 2, 2, 1);

        final IMaster<User> masterConfigForUser = new SimpleMasterBuilder<User>()
                .forEntity(User.class)
                .addProp("key").asSinglelineText().also()
                .addProp("basedOnUser").asAutocompleter().withMatcher(UserMasterBaseUserMatcher.class).also()
                .addProp(ACTIVE).asCheckbox().also()
                .addProp("base").asCheckbox().also()
                .addProp(EMAIL).asSinglelineText().also()
                .addProp(SSO_ONLY).asCheckbox().also()
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
    
    private EntityCentre<UserAndRoleAssociation> createUserAndRoleAssociationCentre(final Injector injector) {
        final Class<UserAndRoleAssociation> root = UserAndRoleAssociation.class;
        final String layout = LayoutComposer.mkVarGridForCentre(1);

        final EntityActionConfig standardEditAction = StandardActions.EDIT_ACTION.mkAction(UserAndRoleAssociation.class);
        final EntityActionConfig standardNewAction = StandardActions.NEW_WITH_MASTER_ACTION.mkAction(UserAndRoleAssociation.class);
        final EntityActionConfig standardDeleteAction = StandardActions.DELETE_ACTION.mkAction(UserAndRoleAssociation.class);
        final EntityActionConfig standardExportAction = StandardActions.EXPORT_EMBEDDED_CENTRE_ACTION.mkAction(UserAndRoleAssociation.class);
        final EntityActionConfig standardSortAction = CentreConfigActions.CUSTOMISE_COLUMNS_ACTION.mkAction();

        final EntityCentreConfig<UserAndRoleAssociation> ecc = EntityCentreBuilder.centreFor(root)
                .runAutomatically()
                .addTopAction(standardNewAction).also()
                .addTopAction(standardDeleteAction).also()
                .addTopAction(standardSortAction).also()
                .addTopAction(standardExportAction)
                .addCrit("userRole").asMulti().autocompleter(UserRole.class)
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .addProp("userRole").order(1).asc().width(80)
                    .withSummary("total_count_", "COUNT(SELF)", "Count:The total number of matching Roles.").also()
                .addProp("userRole.desc").minWidth(80)
                .addPrimaryAction(standardEditAction)
                .setQueryEnhancer(UserMaster_UserAndRoleAssociationCentre_QueryEnhancer.class, context().withMasterEntity().build())
                .build();

        return new EntityCentre<>(MiUserMaster_UserAndRoleAssociation.class, ecc, injector);
    }

    private static class UserMaster_UserAndRoleAssociationCentre_QueryEnhancer implements IQueryEnhancer<UserAndRoleAssociation> {
        @Override
        public ICompleted<UserAndRoleAssociation> enhanceQuery(final IWhere0<UserAndRoleAssociation> where, final Optional<CentreContext<UserAndRoleAssociation, ?>> context) {
            return enhanceEmbededCentreQuery(where, createConditionProperty("user"), context.get().getMasterEntity().getKey());
        }
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
                        .preAction(entityNavigation("User"))
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
