package ua.com.fielden.platform.web.resources.webui;

import com.google.inject.Injector;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.EntityDeleteAction;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.security.user.*;
import ua.com.fielden.platform.tiny.PlatformActionIdentifiers;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.security.user.UserRoleProducer;
import ua.com.fielden.platform.ui.menu.sample.MiUserRole;
import ua.com.fielden.platform.web.PrefDim.Unit;
import ua.com.fielden.platform.web.action.CentreConfigurationWebUiConfig.CentreConfigActions;
import ua.com.fielden.platform.web.action.pre.EntityNavigationPreAction;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.layout.api.impl.FlexLayoutConfig;
import ua.com.fielden.platform.web.test.server.config.StandardActions;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

import java.util.Optional;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.security.user.CopyUserRoleAction.*;
import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.action.pre.ConfirmationPreAction.okCancel;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.cell;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutCellBuilder.layout;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.mkActionLayoutForMaster;

/**
 * {@link UserRole} Web UI configuration.
 *
 * @author TG Team
 *
 */
import java.util.Optional;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.action.pre.ConfirmationPreAction.okCancel;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.cell;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutCellBuilder.layout;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutComposer.mkActionLayoutForMaster;

/// [UserRole] Web UI configuration.
///
/// @author TG Team
public class UserRoleWebUiConfig {

    public final EntityMaster<CopyUserRoleAction> copyUserRoleActionMaster;
    public final EntityCentre<UserRole> centre;
    public final EntityMaster<UserRole> master;

    public static UserRoleWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new UserRoleWebUiConfig(injector);
    }

    private UserRoleWebUiConfig(final Injector injector) {
        centre = createCentre(injector);
        master = createMaster(injector);
        copyUserRoleActionMaster = copyUserRoleActionMaster(injector);
    }

    /**
     * Creates entity centre for {@link UserRole}.
     *
     * @return
     */
    private static EntityCentre<UserRole> createCentre(final Injector injector) {
        final String fmr = "'flex', 'margin-right: 20px', 'width: 200px'";
        final String fmrLast = "'flex', 'width: 200px'";
        final String critLayout = "['vertical', 'center-justified', "
                + format("[[%s], [%s]], ", fmr, fmrLast)
                + format("['flex']")
                + "]";

        return new EntityCentre<>(MiUserRole.class, "User Roles",
                EntityCentreBuilder.centreFor(UserRole.class)
                .runAutomatically()
                .addTopAction(UserRoleActions.NEW_ACTION.mkAction()).also()
                .addTopAction(UserRoleActions.DELETE_ACTION.mkAction()).also()
                .addTopAction(CentreConfigActions.CUSTOMISE_COLUMNS_ACTION.mkAction()).also()
                .addTopAction(StandardActions.EXPORT_ACTION.mkAction(UserRole.class)).also()
                .addTopAction(UserRoleActions.COPY_ACTION.mkAction())
                .addCrit("this").asMulti().autocompleter(UserRole.class).also()
                .addCrit(ActivatableAbstractEntity.ACTIVE).asMulti().bool().also()
                .addCrit("desc").asMulti().text()
                .setLayoutFor(Device.DESKTOP, Optional.empty(), critLayout)
                .addProp("this")
                    .order(1).asc()
                    .width(200)
                .also()
                .addProp("desc").minWidth(200).also()
                .addProp(ACTIVE).minWidth(50)
                .addPrimaryAction(UserRoleActions.EDIT_ACTION.mkAction())
                .build(), injector, null);
    }

    /// Creates entity master for [UserRole].
    ///
    private static EntityMaster<UserRole> createMaster(final Injector injector) {
        final int MARGIN = 20;
        final String MARGIN_PIX = MARGIN + "px";
        final FlexLayoutConfig CELL_LAYOUT = layout().flex().end();
        final FlexLayoutConfig FLEXIBLE_ROW = layout().flexAuto().end();
        final FlexLayoutConfig FLEXIBLE_LAYOUT_WITH_PADDING = layout()
                .withStyle("height", "100%")
                .withStyle("box-sizing", "border-box")
                .withStyle("min-height", "fit-content")
                .withStyle("padding", MARGIN_PIX).end();

        final String layout = cell(
                cell(cell(CELL_LAYOUT).repeat(2).withGapBetweenCells(MARGIN)).
                cell(cell(CELL_LAYOUT), FLEXIBLE_ROW),
                FLEXIBLE_LAYOUT_WITH_PADDING).toString();

        final IMaster<UserRole> masterConfigForUserRole = new SimpleMasterBuilder<UserRole>()
                .forEntity(UserRole.class)
                .addProp(KEY).asSinglelineText().also()
                .addProp(ACTIVE).asCheckbox().also()
                .addProp(DESC).asMultilineText().also()
                .addAction(MasterActions.REFRESH).shortDesc("CANCEL").longDesc("Cancel changes")
                .addAction(MasterActions.SAVE).longDesc("Save changes")
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), mkActionLayoutForMaster())
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .withDimensions(mkDim(480, 320))
                .done();
        return new EntityMaster<>(
                UserRole.class,
                UserRoleProducer.class,
                masterConfigForUserRole,
                injector);
    }

    private static EntityMaster<CopyUserRoleAction> copyUserRoleActionMaster(final Injector injector) {
        final int MARGIN = 20;
        final String MARGIN_PIX = MARGIN + "px";
        final FlexLayoutConfig CELL_LAYOUT = layout().flex().end();
        final FlexLayoutConfig FLEXIBLE_ROW = layout().flexAuto().end();
        final FlexLayoutConfig FLEXIBLE_LAYOUT_WITH_PADDING = layout()
                .withStyle("height", "100%")
                .withStyle("box-sizing", "border-box")
                .withStyle("min-height", "fit-content")
                .withStyle("padding", MARGIN_PIX).end();

        final String layout = cell(
                cell(cell(CELL_LAYOUT).repeat(2).withGapBetweenCells(MARGIN)).
                        cell(cell(CELL_LAYOUT), FLEXIBLE_ROW),
                FLEXIBLE_LAYOUT_WITH_PADDING).toString();

        final var master = new SimpleMasterBuilder<CopyUserRoleAction>()
                .forEntity(CopyUserRoleAction.class)
                .addProp(ROLE_TITLE).asSinglelineText().also()
                .addProp(ROLE_ACTIVE).asCheckbox().also()
                .addProp(ROLE_DESC).asMultilineText().also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
                .addAction(MasterActions.SAVE).shortDesc("Create").longDesc("Create %s".formatted(UserRole.ENTITY_TITLE))
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), mkActionLayoutForMaster())
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .withDimensions(mkDim(480, 320))
                .done();
        return new EntityMaster<>(CopyUserRoleAction.class, CopyUserRoleActionProducer.class, master, injector);
    }

    private enum UserRoleActions {

        NEW_ACTION {
            @Override
            public EntityActionConfig mkAction() {
                return action(EntityNewAction.class)
                        .withContext(context().withSelectionCrit().build())
                        .icon("add-circle-outline")
                        .shortDesc("Add new User Role")
                        .longDesc("Initiates creation of a new User Role.")
                        .shortcut("alt+n")
                        .build();
            }

        },

        EDIT_ACTION {
            @Override
            public EntityActionConfig mkAction() {
                return action(EntityEditAction.class)
                        .withContext(context().withCurrentEntity().withSelectionCrit().build())
                        .preAction(new EntityNavigationPreAction("User Role"))
                        .icon("editor:mode-edit")
                        .shortDesc("Edit User Role")
                        .longDesc("Opens master for User Role editing.")
                        .withNoParentCentreRefresh()
                        .build();
            }

        },

        DELETE_ACTION {
            @Override
            public EntityActionConfig mkAction() {
                final String desc = "Delete selected user role(s).";
                return action(EntityDeleteAction.class)
                        .withContext(context().withSelectedEntities().build())
                        .preAction(okCancel("Please confirm whether the selected user role(s) should be deleted?"))
                        .icon("remove-circle-outline")
                        .shortDesc(desc)
                        .longDesc(desc)
                        .shortcut("alt+d")
                        .build();
            }
        },

        COPY_ACTION {
            @Override
            public EntityActionConfig mkAction() {
                return action(CopyUserRoleAction.class)
                        .withTinyHyperlink(PlatformActionIdentifiers.PLATFORM_COPY_USER_ROLE)
                        .withContext(context().withSelectedEntities().build())
                        .icon("icons:content-copy")
                        .shortDesc("Add new User Role from selected roles")
                        .longDesc("Creates a new User Role with all security tokens of selected roles.")
                        .build();
            }
        };

        public abstract EntityActionConfig mkAction();
    }

}
