package ua.com.fielden.platform.swing.usertable;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;

import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
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

    public UserReviewModel(final IUserController userController) {
	this.userTableModel = new UserTableModel();
	this.userController = userController;
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
		final List<? extends User> users = userController.findAllUsersWithRoles();
		for (final User user : users) {
		    userController.updateUser(user, userTableModel.getUserRolesFor(user, true));
		}
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
     * @return
     */
    private Action createLoadAction(final BlockingIndefiniteProgressLayer pane) {
	final Action action = new BlockingLayerCommand<Pair<List<? extends User>, List<? extends UserRole>>>("Load", pane) {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		final boolean result = super.preAction();
		setMessage("Loading...");
		return result;
	    }

	    @Override
	    protected Pair<List<? extends User>, List<? extends UserRole>> action(final ActionEvent e) throws Exception {
		final List<? extends User> users = userController.findAllUsers();
		return new Pair<List<? extends User>, List<? extends UserRole>>(users, userController.findAllUserRoles());
	    }

	    @Override
	    protected void postAction(final Pair<List<? extends User>, List<? extends UserRole>> value) {
		userTableModel.loadData(value.getKey(), value.getValue());
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
    public Action getLoadAction(final BlockingIndefiniteProgressLayer pane) {
	if (loadAction == null) {
	    loadAction = createLoadAction(pane);
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
