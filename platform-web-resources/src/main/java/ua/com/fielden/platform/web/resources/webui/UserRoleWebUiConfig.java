package ua.com.fielden.platform.web.resources.webui;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.web.PrefDim.mkDim;
import static ua.com.fielden.platform.web.action.StandardMastersWebUiConfig.MASTER_ACTION_SPECIFICATION;
import static ua.com.fielden.platform.web.action.pre.ConfirmationPreAction.okCancel;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutBuilder.cell;
import static ua.com.fielden.platform.web.layout.api.impl.LayoutCellBuilder.layout;

import java.util.Optional;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.EntityDeleteAction;
import ua.com.fielden.platform.entity.EntityEditAction;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.security.user.UserRoleProducer;
import ua.com.fielden.platform.security.user.UserRoleTokensUpdater;
import ua.com.fielden.platform.security.user.UserRoleTokensUpdaterProducer;
import ua.com.fielden.platform.ui.menu.sample.MiUserRole;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.layout.api.impl.FlexLayoutConfig;
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
    private static final String actionButton = MASTER_ACTION_SPECIFICATION;
    private static final String bottomButtonPanel = "['horizontal', 'padding: 20px', 'justify-content: center', 'wrap', [%s], [%s]]";

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
        final String fmr = "'flex', 'margin-right: 20px', 'width: 200px'";
        final String fmrLast = "'flex', 'width: 200px'";
        final String critLayout = "['vertical', 'center-justified', "
                + format("[[%s], [%s]], ", fmr, fmrLast)
                + format("['flex']")
                + "]";

        final EntityCentre<UserRole> userRoleCentre = new EntityCentre<>(MiUserRole.class, "User Roles",
                EntityCentreBuilder.centreFor(UserRole.class)
                .runAutomatically()
                .addTopAction(UserRoleActions.NEW_ACTION.mkAction()).also()
                .addTopAction(UserRoleActions.DELETE_ACTION.mkAction())
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
                .addPrimaryAction(UserRoleActions.EDIT_ACTION.mkAction()).also()
                .addSecondaryAction(UserRoleActions.MANAGE_SECURITY_TOKEN_ACTION.mkAction())
                .build(), injector, null);
        return userRoleCentre;
    }

    /**
     * Creates entity master for {@link UserRole}.
     *
     * @return
     */
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
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), format(bottomButtonPanel, actionButton, actionButton))
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .withDimensions(mkDim(480, 320))
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
                .addProp("tokens").asCollectionalEditor().withHeader("title")
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
        return new EntityMaster<UserRoleTokensUpdater>(
                UserRoleTokensUpdater.class,
                UserRoleTokensUpdaterProducer.class,
                masterConfig,
                injector);
    }

    private static enum UserRoleActions {

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
                        .icon("editor:mode-edit")
                        .shortDesc("Edit User Role")
                        .longDesc("Opens master for User Role editing.")
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

        MANAGE_SECURITY_TOKEN_ACTION {
            @Override
            public EntityActionConfig mkAction() {
                return action(UserRoleTokensUpdater.class)
                        .withContext(context().withCurrentEntity().build())
                        .icon("av:playlist-add-check")
                        .shortDesc("Add/Remove Security Tokens")
                        .longDesc("Add/remove security token associations for the current user role.")
                        .build();
            }

        };

        public abstract EntityActionConfig mkAction();
    }
}
