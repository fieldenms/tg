package ua.com.fielden.platform.swing.usertable;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JOptionPane;

import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.pagination.IPageChangedListener;
import ua.com.fielden.platform.pagination.IPageNavigationListener;
import ua.com.fielden.platform.pagination.IPaginatorModel.PageNavigationPhases;
import ua.com.fielden.platform.pagination.PageChangedEvent;
import ua.com.fielden.platform.pagination.PageHolder;
import ua.com.fielden.platform.pagination.PageNavigationEvent;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.utils.Pair;

/**
 * Model for the {@link UserReview} panel.
 * 
 * @author TG Team
 * 
 */
public class UserReviewModel {

    private Action loadAction;
    private Action saveAction;

    private final UserTableModel userTableModel;

    private final IUserController userController;

    /**
     * Holds the current page of data.
     */
    private final PageHolder pageHolder;

    public UserReviewModel(final IUserController userController) {
        this.userTableModel = new UserTableModel();
        this.userController = userController;
        this.pageHolder = new PageHolder();
        this.pageHolder.addPageChangedListener(createUserDataChangedListener());
        this.pageHolder.addPageNavigationListener(createPageNavigationListener());
    }

    private IPageNavigationListener createPageNavigationListener() {
        return new IPageNavigationListener() {

            private final Map<User, Set<UserRole>> changedAssociations = new HashMap<>();

            @Override
            public boolean pageNavigated(final PageNavigationEvent event) {
                if (PageNavigationPhases.PRE_NAVIGATE.equals(event.getPageNavigationPhases())) {
                    changedAssociations.clear();
                    changedAssociations.putAll(getChangedUserRoleAssociations());
                    if (!changedAssociations.isEmpty()) {
                        final int optionResult = JOptionPane.showOptionDialog(null, //
                                "The user/roles associations were changed. Would you like to save before continue?",//
                                "User/role associations changed", //
                                JOptionPane.YES_NO_CANCEL_OPTION, //
                                JOptionPane.QUESTION_MESSAGE, null, null, null);
                        if (optionResult == JOptionPane.NO_OPTION) {
                            changedAssociations.clear();
                        } else if (optionResult == JOptionPane.CANCEL_OPTION || optionResult == JOptionPane.CLOSED_OPTION) {
                            changedAssociations.clear();
                            return false;
                        }
                    }
                } else if (PageNavigationPhases.NAVIGATE.equals(event.getPageNavigationPhases())) {
                    if (!changedAssociations.isEmpty()) {
                        userController.updateUsers(changedAssociations);
                        changedAssociations.clear();
                    }
                }
                return true;
            }
        };
    }

    private IPageChangedListener createUserDataChangedListener() {
        return new IPageChangedListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void pageChanged(final PageChangedEvent e) {
                SwingUtilitiesEx.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        userTableModel.setUsers((List<? extends User>) e.getNewPage().data());
                    }
                });
            }
        };
    }

    /**
     * Returns the {@link PageHolder} instance that holds the currently viewed page.
     * 
     * @return
     */
    public PageHolder getPageHolder() {
        return pageHolder;
    }

    /**
     * Returns the map between user and set of associated user roles those were changed.
     * 
     * @return
     */
    private Map<User, Set<UserRole>> getChangedUserRoleAssociations() {
        final Map<User, Set<UserRole>> userRolesMap = new HashMap<>();
        for (final User user : userTableModel.getUsers()) {
            final Set<UserRole> newRoles = userTableModel.getUserRolesFor(user, true);
            if (!user.roles().equals(newRoles)) {
                userRolesMap.put(user, newRoles);
            }
        }
        return userRolesMap;
    }

    /**
     * Returns the action that saves the data from the {@link UserTableModel}
     * 
     * @param pane
     * @return
     */
    private Action createSaveAction(final BlockingIndefiniteProgressLayer pane) {
        final Action action = new BlockingLayerCommand<Void>("Save", pane) {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean preAction() {
                final boolean result = super.preAction();
                setMessage("Saving...");
                return result;
            }

            @Override
            protected Void action(final ActionEvent e) throws Exception {
                final Map<User, Set<UserRole>> userRolesMap = getChangedUserRoleAssociations();
                if (!userRolesMap.isEmpty()) {
                    userController.updateUsers(userRolesMap);
                }
                for (final Map.Entry<User, Set<UserRole>> mapEntry : userRolesMap.entrySet()) {
                    mapEntry.getKey().setRoles(createAssociationsFor(mapEntry.getKey(), mapEntry.getValue()));
                }
                return null;
            }

            /**
             * Creates the associations between user and given user roles.
             * 
             * @param user
             * @param roles
             * @return
             */
            private Set<UserAndRoleAssociation> createAssociationsFor(final User user, final Set<UserRole> roles) {
                final Set<UserAndRoleAssociation> associations = new HashSet<>();
                for (final UserRole role : roles) {
                    associations.add(new UserAndRoleAssociation(user, role));
                }
                return associations;
            }

        };
        action.putValue(Action.SHORT_DESCRIPTION, "Saves changes.");
        action.setEnabled(true);
        return action;
    }

    /**
     * Returns the action that loads data into the {@link UserTableModel}
     * 
     * @param pane
     * @param review
     * @return
     */
    private Action createLoadAction(final UserReview review) {
        final Action action = new BlockingLayerCommand<Pair<IPage<? extends User>, List<? extends UserRole>>>("Load", review) {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean preAction() {
                final boolean result = super.preAction();
                setMessage("Loading...");
                return result;
            }

            @Override
            protected Pair<IPage<? extends User>, List<? extends UserRole>> action(final ActionEvent e) throws Exception {
                final IPage<? extends User> users = userController.firstPageOfUsersWithRoles(review.getPageSize());
                return new Pair<IPage<? extends User>, List<? extends UserRole>>(users, userController.findAllUserRoles());
            }

            @Override
            protected void postAction(final Pair<IPage<? extends User>, List<? extends UserRole>> value) {
                userTableModel.setRoles(value.getValue());
                pageHolder.newPage(value.getKey());
                super.postAction(value);
            }

        };
        action.putValue(Action.SHORT_DESCRIPTION, "Loads data.");
        action.setEnabled(true);
        return action;
    }

    /**
     * Returns the {@link UserTableModel} instance
     * 
     * @return
     */
    public UserTableModel getUserTableModel() {
        return userTableModel;
    }

    /**
     * Returns the {@link IUserController} that is responsible for retrieving and saving data
     * 
     * @return
     */
    public IUserController getUserController() {
        return userController;
    }

    /**
     * See {@link #createLoadAction(BlockingIndefiniteProgressLayer)}
     * 
     * @param pane
     * @return
     */
    public Action getLoadAction(final UserReview review) {
        if (loadAction == null) {
            loadAction = createLoadAction(review);
        }
        return loadAction;
    }

    /**
     * See {@link #createSaveAction(BlockingIndefiniteProgressLayer)}
     * 
     * @param pane
     * @return
     */
    public Action getSaveAction(final BlockingIndefiniteProgressLayer pane) {
        if (saveAction == null) {
            saveAction = createSaveAction(pane);
        }
        return saveAction;
    }
}
