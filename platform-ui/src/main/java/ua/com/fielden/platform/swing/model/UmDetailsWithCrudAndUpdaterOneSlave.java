package ua.com.fielden.platform.swing.model;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.Action;

import ua.com.fielden.platform.dao2.IMasterDetailsDao2;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.NotificationLayer.MessageType;
import ua.com.fielden.platform.swing.components.bind.development.Binder;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.swing.dialogs.DialogWithDetails;
import ua.com.fielden.platform.swing.ei.editors.development.ILightweightPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.development.IPropertyEditor;

/**
 * This is a CRUD UI model representing one-to-one association, which acts as a slave model the the model passed into the constructor.
 * <p>
 * Affectively the only reason to use this model is when one needs to have the same entity instance to be represented on different views. For example, vehicle technical details are
 * can be managed this way where transmission and engine related properties needs to be displayed and edited on different views.
 * <p>
 * IMPORTANT: In most cases it is best to split a complex entity requiring multiple views into several smaller entity classes, which can be managed view ordinary one-to-one CRUD
 * model.
 *
 * @author TG Team
 *
 * @param <M>
 * @param <D>
 * @param <C>
 */
public abstract class UmDetailsWithCrudAndUpdaterOneSlave<M extends AbstractEntity, D extends AbstractEntity, C extends IMasterDetailsDao2<M, D>> extends UmDetailsWithCrudAndUpdaterOne<M, D, C> {

    private final UmDetailsWithCrudAndUpdaterOne<M, D, C> masterModel;

    protected UmDetailsWithCrudAndUpdaterOneSlave(final UmDetailsWithCrudAndUpdaterOne<M, D, C> masterModel) {
	super(masterModel.getEntity(), masterModel.getController(), masterModel.getPropertyBinder(), masterModel.getFetchModel(), masterModel.getTitleUpdater());
	this.masterModel = masterModel;
    }

    /** A convenient method for loading details entity. Could potentially be overridden, but unlikely there should be a reason for that. */
    @Override
    protected D loadDetails() {
	return getMasterModel().loadDetails();
    }

    @Override
    protected D newEntity(final EntityFactory factory) {
	return getMasterModel().newEntity(factory);
    }

    //////////////////////////////////////////////////////////////
    /////////////////////// INITIALISATION ///////////////////////
    //////////////////////////////////////////////////////////////
    @Override
    protected void preInit(final BlockingIndefiniteProgressPane blockingPane) {
	getMasterModel().preInit(blockingPane);
    }

    private D tmpDetails;

    @Override
    protected void doInit(final BlockingIndefiniteProgressPane blockingPane) {
	if (!getMasterModel().isInitialised()) {
	    tmpDetails = loadDetails();
	}
    }

    @Override
    protected void postInit(final BlockingIndefiniteProgressPane blockingPane) {
	if (!getMasterModel().isInitialised()) {
	    getMasterModel().setEditors(getMasterModel().buildEditors(getEntity(), getController(), getPropertyBinder()));
	}
	getView().buildUi();

	if (!getMasterModel().isInitialised()) {
	    setManagedEntity(tmpDetails);
	}

	setState(UmState.VIEW);
    }

    @Override
    public void setEntity(final M entity) {
	getMasterModel().setEntity(entity);
    }

    @Override
    public M getEntity() {
	return getMasterModel().getEntity();
    };

    @Override
    protected void setManagedEntity(final D entity) {
	getMasterModel().setManagedEntity(entity);
    }

    @Override
    protected D getManagedEntity() {
	return getMasterModel().getManagedEntity();
    }

    @Override
    public void setState(final UmState state) {
	super.setState(state);
	getMasterModel().setState(state);
    }

    @Override
    public UmState getState() {
	return getMasterModel().getState();
    }

    @Override
    protected void setInitialised(final boolean initialised) {
	getMasterModel().setInitialised(initialised);
	super.setInitialised(initialised);
    }

    @Override
    protected Map<String, IPropertyEditor> buildEditors(final M entity, final C controller, final ILightweightPropertyBinder<D> propertyBinder) {
	throw new IllegalStateException("Slave model should not build editors.");
    };

    @Override
    public Map<String, IPropertyEditor> getEditors() {
	return getMasterModel().getEditors();
    }

    @Override
    protected void disableEditors(final boolean flag) {
	// do not do anything because editors are disabled at the master model level
    }

    //////////////////////////////////////////////////////////////
    ////////////////////////// ACTIONS ///////////////////////////
    //////////////////////////////////////////////////////////////
    @Override
    protected Action createCancelAction() {
	final Command<D> action = new Command<D>("Cancel") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		notifyActionStageChange(ActionStage.CANCEL_PRE_ACTION);
		setState(UmState.UNDEFINED);
		return super.preAction();
	    }

	    @Override
	    protected D action(final ActionEvent arg0) throws Exception {
		notifyActionStageChange(ActionStage.CANCEL_ACTION);
		return loadDetails();
	    }

	    @Override
	    protected void postAction(final D entity) {
		setManagedEntity(entity);
		if (getView() != null) {
		    getView().notify("", MessageType.NONE);
		}
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
		setState(UmState.UNDEFINED);
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
		notifyActionStageChange(ActionStage.REFRESH_ACTION);
		// wait before the commit process finish
		getManagedEntity().isValid();
		return loadDetails();

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
    protected Action createSaveAction() {
	final Command<Result> action = new Command<Result>("Save") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		notifyActionStageChange(ActionStage.SAVE_PRE_ACTION);
		setState(UmState.UNDEFINED);
		return super.preAction();
	    }

	    @Override
	    protected Result action(final ActionEvent arg0) throws Exception {
		notifyActionStageChange(ActionStage.SAVE_ACTION);
		try {
		    // wait before the commit process finish
		    final Result result = getManagedEntity().isValid();
		    if (result.isSuccessful()) {
			setManagedEntity(getController().saveDetails(getEntity(), getManagedEntity()));
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

    protected UmDetailsWithCrudAndUpdaterOne<M, D, C> getMasterModel() {
	return masterModel;
    }
}
