package ua.com.fielden.platform.swing.model;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import ua.com.fielden.platform.dao.IMasterDetailsDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.NotificationLayer.MessageType;
import ua.com.fielden.platform.swing.components.bind.development.Binder;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.swing.dialogs.DialogWithDetails;
import ua.com.fielden.platform.swing.ei.editors.development.ILightweightPropertyBinder;
import ua.com.fielden.platform.swing.utils.Dialogs;

/**
 * This is a CRUD UI model representing one-to-one master/details association.
 *
 * @author TG Team
 *
 * @param <M>
 * @param <D>
 * @param <C>
 */
public abstract class UmDetailsWithCrudOne<M extends AbstractEntity<?>, D extends AbstractEntity<?>, C extends IMasterDetailsDao<M, D>> extends UmDetailsWithCrud<M, D, C> {

    protected UmDetailsWithCrudOne(final M entity, final C companion, final ILightweightPropertyBinder<D> propertyBinder, final fetch<D> fm) {
	super(entity, companion, propertyBinder, fm, true);
    }

    /** A convenient method for loading details entity. Could potentially be overridden, but unlikely there should be a reason for that. */
    protected D loadDetails() {
	final List<D> list = getCompanion().findDetails(getEntity(), getFetchModel(), Integer.MAX_VALUE).data();
	// there should really be just one or zero elements; if there are more this is an incorrect model to be used or the data is corrupted.
	if (list.size() == 1) {
	    return list.get(0);
	} else if (list.size() == 0) {
	    return newEntity(getEntity().getEntityFactory());
	}

	throw new IllegalArgumentException("There is mo than one entity instance: either data is corrupted or the use of this model is incorrect.");
    }

    //////////////////////////////////////////////////////////////
    /////////////////////// INITIALISATION ///////////////////////
    //////////////////////////////////////////////////////////////
    /** A field for temporal holding of details entity retrieved upon initialisation. */
    private D tmpDetails;

    @Override
    protected void preInit(final BlockingIndefiniteProgressPane blockingPane) {
	blockingPane.setText("Loading...");
    }

    @Override
    protected void doInit(final BlockingIndefiniteProgressPane blockingPane) {
	tmpDetails = loadDetails();
    }

    @Override
    protected void postInit(final BlockingIndefiniteProgressPane blockingPane) {
	setEditors(buildEditors(getEntity(), getCompanion(), getPropertyBinder()));
	getView().buildUi();

	setManagedEntity(tmpDetails);

	setState(UmState.VIEW);
    }

    //////////////////////////////////////////////////////////////
    ////////////////////////// ACTIONS ///////////////////////////
    //////////////////////////////////////////////////////////////
    /**
     * Adding a new entity to one-to-one association is incorrect and thus not supported.
     */
    @Override
    protected Action createNewAction() {
	return new AbstractAction("") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		throw new UnsupportedOperationException("Adding a new entity to one-to-one association is incorrect and thus not supported.");
	    }
	};
    }

    @Override
    protected Action createCancelAction() {
	final Command<D> action = new Command<D>("Cancel") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		notifyActionStageChange(ActionStage.CANCEL_PRE_ACTION);
		getSaveAction().setEnabled(false);
		return super.preAction();
	    }

	    @Override
	    protected D action(final ActionEvent arg0) throws Exception {
		notifyActionStageChange(ActionStage.CANCEL_ACTION);
		// wait before the commit process finish
		return loadDetails();
	    }

	    @Override
	    protected void postAction(final D entity) {
		if (getView() != null) {
		    getView().notify("", MessageType.NONE);
		}
		setManagedEntity(entity);
		setState(UmState.VIEW);
		notifyActionStageChange(ActionStage.CANCEL_POST_ACTION);
	    }
	};
	action.setEnabled(true);
	action.putValue(Action.SHORT_DESCRIPTION, "Cancel uncommitted changes");
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
	return action;
    }

    @Override
    protected Action createEditAction() {
	final Command<UmState> action = new Command<UmState>("Edit") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		final Result editability = getManagedEntity().isEditable();
		if (!editability.isSuccessful()) {
		    Dialogs.showMessageDialog(getView(), editability.getMessage(), "Edit action", Dialogs.WARNING_MESSAGE);
		    return false;
		}

		getNewAction().setEnabled(false);
		getEditAction().setEnabled(false);
		getSaveAction().setEnabled(true);
		getCancelAction().setEnabled(true);
		getRefreshAction().setEnabled(false);
		notifyActionStageChange(ActionStage.EDIT_PRE_ACTION);
		return super.preAction();
	    }

	    @Override
	    protected UmState action(final ActionEvent arg0) throws Exception {
		notifyActionStageChange(ActionStage.EDIT_ACTION);
		return UmState.EDIT;
	    }

	    @Override
	    protected void postAction(final UmState uiModelState) {
		setState(uiModelState);
		notifyActionStageChange(ActionStage.EDIT_POST_ACTION);
	    }

	};
	action.setEnabled(true);
	action.putValue(Action.SHORT_DESCRIPTION, "Edit");
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
	return action;
    }

    @Override
    protected Action createRefreshAction() {
	final Command<D> action = new Command<D>("Refresh") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		notifyActionStageChange(ActionStage.REFRESH_PRE_ACTION);
		setState(UmState.UNDEFINED);
		Binder.commitFocusOwner();
		return super.preAction();
	    }

	    @Override
	    protected D action(final ActionEvent event) throws Exception {
		try {
		    notifyActionStageChange(ActionStage.REFRESH_ACTION);
		    // wait before the commit process finish
		    getManagedEntity().isValid();
		    return loadDetails();
		} catch (final Exception ex) {
		    setState(UmState.VIEW);
		    throw ex;
		}
	    }

	    @Override
	    protected void postAction(final D entity) {
		setManagedEntity(entity);
		setState(UmState.VIEW);
		notifyActionStageChange(ActionStage.REFRESH_POST_ACTION);
	    }

	};
	action.setEnabled(true);
	action.putValue(Action.SHORT_DESCRIPTION, "Reload entity");
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
	return action;
    }

    @Override
    protected Action createDeleteAction() {
	final Command<D> action = new Command<D>("Delete") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		notifyActionStageChange(ActionStage.DELETE_PRE_ACTION);
		Binder.commitFocusOwner();
		return super.preAction();
	    }

	    @Override
	    protected D action(final ActionEvent event) throws Exception {
		notifyActionStageChange(ActionStage.DELETE_ACTION);
		// wait before the commit process finish
		getManagedEntity().isValid();

		if (getManagedEntity().isPersisted()) {
		    getCompanion().deleteDetails(getEntity(), getManagedEntity());
		}

		return loadDetails();
	    }

	    @Override
	    protected void postAction(final D entity) {
		setManagedEntity(entity);
		setState(UmState.VIEW);
		notifyActionStageChange(ActionStage.DELETE_POST_ACTION);
	    }

	};
	action.setEnabled(true);
	action.putValue(Action.SHORT_DESCRIPTION, "Delete entity");
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);
	return action;
    }

    @Override
    protected Action createSaveAction() {
	final Command<Result> action = new Command<Result>("Save") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		notifyActionStageChange(ActionStage.SAVE_PRE_ACTION);
		getCancelAction().setEnabled(false);
		return super.preAction();
	    }

	    @Override
	    protected Result action(final ActionEvent arg0) throws Exception {
		notifyActionStageChange(ActionStage.SAVE_ACTION);
		try {
		    // wait before the commit process finish
		    final Result result = getManagedEntity().isValid();
		    if (result.isSuccessful()) {
			setManagedEntity(getCompanion().saveDetails(getEntity(), getManagedEntity()));
		    }
		    return result;
		} catch (final Exception ex) {
		    return new Result(getManagedEntity(), ex);
		}
	    }

	    @Override
	    protected void postAction(final Result result) {
		if (result.isSuccessful()) {
		    if (getView() != null) {
			getView().notify("", MessageType.NONE);
		    }
		    setState(UmState.VIEW);
		    notifyActionStageChange(ActionStage.SAVE_POST_ACTION_SUCCESSFUL);
		} else {
		    if (getView() != null) {
			getView().notify(result.getMessage(), MessageType.ERROR);
		    } else {
			new DialogWithDetails(null, "Save", result.getEx()).setVisible(true);
		    }
		    getCancelAction().setEnabled(true);
		    super.postAction(result);
		    notifyActionStageChange(ActionStage.SAVE_POST_ACTION_FAILED);
		}
	    }

	};
	action.setEnabled(true);
	action.putValue(Action.SHORT_DESCRIPTION, "Save changes");
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
	return action;
    }
}
