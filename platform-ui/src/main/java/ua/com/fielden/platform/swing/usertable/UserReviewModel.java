package ua.com.fielden.platform.swing.usertable;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;

import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.pagination.IPageChangedListener;
import ua.com.fielden.platform.pagination.PageChangedEvent;
import ua.com.fielden.platform.pagination.PageHolder;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.User;
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
    }

    private IPageChangedListener createUserDataChangedListener() {
	return new IPageChangedListener() {

	    @SuppressWarnings("unchecked")
	    @Override
	    public void pageChanged(final PageChangedEvent e) {
		SwingUtilitiesEx.invokeLater(new Runnable() {

		    @Override
		    public void run() {
			userTableModel.setUsers((List<? extends User>)e.getNewPage().data());
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
		final Map<User, Set<UserRole>> userRolesMap = new HashMap<>();
		for (final User user : userTableModel.getUsers()) {
		    final Set<UserRole> newRoles = userTableModel.getUserRolesFor(user, true);
		    if(!user.roles().equals(newRoles)) {
			userRolesMap.put(user, newRoles);
		    }
		}
		userController.updateUsers(userRolesMap);
		return null;
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
