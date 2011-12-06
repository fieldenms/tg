package ua.com.fielden.platform.swing.model;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.actions.BlockingCommand;
import ua.com.fielden.platform.swing.components.bind.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.blocking.AdhocBlockingLayerProvider;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.swing.ei.editors.ILightweightPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.IPropertyEditor;
import ua.com.fielden.platform.swing.ei.editors.ReadonlyEntityPropertyViewer;
import ua.com.fielden.platform.swing.view.BaseNotifPanel;

/**
 * This is an attempt to provide a generic UI model implementation, which should provide common functionality for models build around modification of some entity instance.
 *
 * There are two abstract entity based type parameters:
 * <ul>
 * <li>T -- the principle entity type that might or might not be the subject to the model for modification.
 * <li>M -- the managed entity type that represents the type of an entity actually being managed by the model (editing etc.)
 * </ul>
 *
 * In case where T is the same as M, the model manages an entity instance passed into constructor. Otherwise, T and M represent master/details relationship where T is the master
 * (parent) and M is a dependent entity.
 *
 * <p>
 * An important part of the model life cycle is initialisation, which implemented by the final method {@link #init(BlockingIndefiniteProgressPane)}. There are three configuration
 * points of the initialisation process, which are controlled by three methods:
 *
 * <ul>
 * <li>Method {@link #preInit(BlockingIndefiniteProgressPane)} should be used for any UI related logic that should happen before method <code>doInit</code> is invoked. Method
 * <code>preInit</code> is invoked on EDT.
 *
 * <li>Method {@link #doInit(BlockingIndefiniteProgressPane)} should be implemented by descendants in order to provide initialisation logic such as loading of data etc. This method
 * is invoked by {@link #init(BlockingIndefiniteProgressPane)} on a separate thread, and thus should not have any UI related manipulations.
 *
 * <li>Method {@link #postInit(BlockingIndefiniteProgressPane)} should be used for any UI related logic that should happen after method <code>doInit</code> is invoked. For example,
 * for setting a table model based on the data obtained in <code>doInit</code>. Method <code>postInit</code> is invoked on EDT.
 * </ul>
 *
 * Initialisation happens only once and if necessary can be omitted -- constructor's parameter <code>lazy</code> determines whether initialisation is required.
 * <p>
 * IMPORTANT: Model lazy initialisation (this is where lazy is true) should be synchronised with lazy view initialisation -- otherwise view could try to use some property editors,
 * which haven't yet been created. However, the model does not enforce how this should be done, os the implementation details are left to a developer.
 *
 * @author TG Team
 *
 */
public abstract class UModel<M extends AbstractEntity, D extends AbstractEntity, C> implements ICloseGuard, IOpenGuard {

    private Logger logger = Logger.getLogger(this.getClass());

    /**
     * Describes different stages in the execution of model actions.
     *
     */
    public enum ActionStage {
	INIT_PRE_ACTION,
	INIT_ACTION,
	INIT_POST_ACTION,
	CANCEL_PRE_ACTION,
	CANCEL_ACTION,
	CANCEL_POST_ACTION,
	EDIT_PRE_ACTION,
	EDIT_ACTION,
	EDIT_POST_ACTION,
	NEW_PRE_ACTION,
	NEW_ACTION,
	NEW_POST_ACTION,
	REFRESH_PRE_ACTION,
	REFRESH_ACTION,
	REFRESH_POST_ACTION,
	SAVE_PRE_ACTION,
	SAVE_ACTION,
	SAVE_POST_ACTION_SUCCESSFUL,
	SAVE_POST_ACTION_FAILED,
	DELETE_PRE_ACTION,
	DELETE_ACTION,
	DELETE_POST_ACTION,
	CUSTOM_PRE_ACTION,
	CUSTOM_ACTION,
	CUSTOM_POST_ACTION_SUCCESSFUL,
	CUSTOM_POST_ACTION_FAILED
    }

    private M entity;
    private final C controller;
    private final ILightweightPropertyBinder<D> propertyBinder;
    private UmState state = UmState.VIEW;
    private Map<String, IPropertyEditor> editors = new HashMap<String, IPropertyEditor>();

    private final Action newAction;
    private final Action editAction;
    private final Action saveAction;
    private final Action cancelAction;
    private final Action refreshAction;
    private final Action deleteAction;

    /** An instance of view associated with this instance of model, which is used for providing feedback to users. */
    private BaseNotifPanel<? extends UModel<M, D, C>> view;

    /** Indicates whether a lazy model has been initialised or not. */
    private boolean initialised;

    /** Can be used in case a corresponding view has blocking layer */
    protected final AdhocBlockingLayerProvider blockingLayerProvider = new AdhocBlockingLayerProvider();

    /**
     * Primary constructor.
     *
     * @param entity
     *            -- managed or master entity instance.
     * @param controller
     *            -- controller required for performing various entity manipulation as a result of user action.
     * @param propertyBinder
     *            -- property binder, which is used to create bounded editors for a managed entity.
     * @param lazy
     *            -- a flag indicating weather this model should be initialised lazily.
     */
    protected UModel(final M entity, final C controller, final ILightweightPropertyBinder<D> propertyBinder, final boolean lazy) {
	this.entity = entity;
	this.controller = controller;
	this.propertyBinder = propertyBinder;
	this.initialised = !lazy;
	if (!lazy) {
	    this.editors = buildEditors(entity, controller, propertyBinder);
	}

	newAction = createNewAction();
	editAction = createEditAction();
	saveAction = createSaveAction();
	cancelAction = createCancelAction();
	refreshAction = createRefreshAction();
	deleteAction = createDeleteAction();
    }

    /**
     * Should be implemented to provide custom logic upon action notification. Can be useful for update frame title, focusing components etc.
     *
     * @param actionState
     */
    protected abstract void notifyActionStageChange(final ActionStage actionState);

    /**
     * Invokes model initialisation.
     *
     * @param blockingPane
     *            -- the blocking pane to be used to indicate the progress of the initialisation process.
     * @param toBeFocusedAfterInit
     *            -- the component, which should be focused once model has been initialised.
     */
    public final void init(final BlockingIndefiniteProgressPane blockingPane, final JComponent toBeFocusedAfterInit) {
	if (!isInitialised()) {
	    final BlockingCommand<Void> activator = new BlockingCommand<Void>("irrelevant", blockingPane) {
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean preAction() {
		    notifyActionStageChange(ActionStage.INIT_PRE_ACTION);
		    final boolean flag = super.preAction();
		    preInit(blockingPane);
		    return flag;
		}

		@Override
		protected Void action(final ActionEvent e) throws Exception {
		    notifyActionStageChange(ActionStage.INIT_ACTION);
		    doInit(blockingPane);
		    return null;
		}

		@Override
		protected void postAction(final Void value) {
		    try {
			postInit(blockingPane);
			if (toBeFocusedAfterInit != null) {
			    toBeFocusedAfterInit.requestFocusInWindow();
			}
			setInitialised(true);
			super.postAction(value);
		    } finally {
			notifyActionStageChange(ActionStage.INIT_POST_ACTION);
		    }
		}

	    };

	    activator.setMessage("Initialising...");
	    activator.actionPerformed(null);
	} else if (toBeFocusedAfterInit != null) {
	    toBeFocusedAfterInit.requestFocusInWindow();
	}
    }

    /**
     * A convenient method to start model initialisation where no specific component should be focused at the end of the process.
     *
     * @param blockingPane
     */
    public final void init(final BlockingIndefiniteProgressPane blockingPane) {
	init(blockingPane, null);
    }

    /**
     * Should be implemented if pre-initialisation UI logic is required. For example, setting up a message on the blocking panel.
     *
     * @param blockingPane
     */
    protected void preInit(final BlockingIndefiniteProgressPane blockingPane) {
    }

    /**
     * Should be implemented if initialisation logic is required. For example, loading of data.
     *
     * @param blockingPane
     */
    protected void doInit(final BlockingIndefiniteProgressPane blockingPane) {
    }

    /**
     * Should be implemented if post-initialisation UI logic is required. For example, setting up a table model based on the retrieved in method <code>doInit</code> data.
     *
     * @param blockingPane
     */
    protected void postInit(final BlockingIndefiniteProgressPane blockingPane) {
    }

    /**
     * Changes model state, rebinds property editors and enables/disables standard actions.
     * <p>
     * Can be overridden if some custom behaviour is required. Super call to this implementation is highly advised.
     *
     * @param state
     */
    protected void setState(final UmState state) {
	this.state = state;
	switch (state) {
	case UNDEFINED:
	    newAction.setEnabled(false);
	    editAction.setEnabled(false);
	    saveAction.setEnabled(false);
	    cancelAction.setEnabled(false);
	    refreshAction.setEnabled(false);
	    deleteAction.setEnabled(false);
	    break;
	case VIEW:
	    getPropertyBinder().rebind(getEditors(), getManagedEntity());
	    disableEditors(true);
	    newAction.setEnabled(true);
	    editAction.setEnabled(canEdit());
	    saveAction.setEnabled(false);
	    cancelAction.setEnabled(false);
	    refreshAction.setEnabled(true);
	    deleteAction.setEnabled(true);
	    break;
	case NEW:// falls through to EDIT
	case EDIT:
	    getPropertyBinder().rebind(getEditors(), getManagedEntity());
	    disableEditors(false);
	    newAction.setEnabled(false);
	    editAction.setEnabled(false);
	    saveAction.setEnabled(true);
	    cancelAction.setEnabled(true);
	    refreshAction.setEnabled(false);
	    deleteAction.setEnabled(false);
	    break;
	}
    }

    protected boolean canEdit() {
	return entity.isEditable().isSuccessful();
    }

    protected abstract Map<String, IPropertyEditor> buildEditors(M entity, C controller, final ILightweightPropertyBinder<D> propertyBinder);

    protected abstract D getManagedEntity();

    protected abstract Action createNewAction();

    protected abstract Action createEditAction();

    protected abstract Action createSaveAction();

    protected abstract Action createCancelAction();

    protected abstract Action createRefreshAction();

    protected abstract Action createDeleteAction();

    @Override
    public ICloseGuard canClose() {
	return state == UmState.VIEW ? null : this;
    }

    @Override
    public void close() {
	// do nothing by default
    }

    @Override
    public boolean canLeave() {
	return canClose() == null;
    }

    @Override
    public String whyCannotClose() {
	return "Please save or cancel changes.";
    }

    @Override
    public boolean canOpen() {
	return true;
    }

    @Override
    public String whyCannotOpen() {
	return "default implementation: should have been overridden";
    }

    protected void disableEditors(final boolean flag) {
	for (final Map.Entry<String, IPropertyEditor> pair : editors.entrySet()) {
	    if (pair.getValue() instanceof ReadonlyEntityPropertyViewer) { // property viewer does not require to be disabled/enabled
		pair.getValue().getEditor().setEnabled(true);
	    } else { // other editor type require further processing
		logger.debug((flag ? "Disabling" : "Enabling") + " editor " + pair.getValue().getLabel().getToolTipText());

		if (!getManagedEntity().getProperty(pair.getKey()).isEditable()) { // readonly editors should stay that way
		    searchAndDisable(pair.getValue().getEditor(), true);
		} else {
		    searchAndDisable(pair.getValue().getEditor(), flag);
		}
	    }
	}
    }

    /**
     * Traverses the component tree of the <code>topComponent</code> in search for the {@link BoundedValidationLayer} to disable/enable it.
     *
     * @param topComponent
     * @param flag
     */
    private void searchAndDisable(final Component topComponent, final boolean flag) {
	if (topComponent instanceof BoundedValidationLayer) {
	    topComponent.setEnabled(!flag);
	} else if (topComponent instanceof JComponent) {
	    final JComponent jtopComponent = (JComponent) topComponent;
	    for (final Component component : jtopComponent.getComponents()) {
		searchAndDisable(component, flag);
	    }
	}
    }

    public M getEntity() {
	return entity;
    }

    /**
     * Ensures that master entity is set and the model is reinitialised if required by triggering the refresh action.
     */
    public void setEntity(final M entity) {
	this.entity = entity;
    }

    public C getController() {
	return controller;
    }

    public ILightweightPropertyBinder<D> getPropertyBinder() {
	return propertyBinder;
    }

    public UmState getState() {
	return state;
    }

    public Map<String, IPropertyEditor> getEditors() {
	return Collections.unmodifiableMap(editors);
    }

    /**
     * Add a rebindable label of the specified property (support dot-notation) to the map of editors.
     *
     * @param dotNotatatedPropertyName
     */
    protected final void addPropertyViewer(final Map<String, IPropertyEditor> map, final String dotNotatatedPropertyName) {
	map.put(dotNotatatedPropertyName, new ReadonlyEntityPropertyViewer(getManagedEntity(), dotNotatatedPropertyName));
    }

    public Action getNewAction() {
	return newAction;
    }

    public Action getEditAction() {
	return editAction;
    }

    public Action getSaveAction() {
	return saveAction;
    }

    public Action getCancelAction() {
	return cancelAction;
    }

    public Action getRefreshAction() {
	return refreshAction;
    }

    public void setEditors(final Map<String, IPropertyEditor> editors) {
	this.editors = editors;
    }

    public BaseNotifPanel<? extends UModel<M, D, C>> getView() {
	return view;
    }

    public void setView(final BaseNotifPanel<? extends UModel<M, D, C>> view) {
	this.view = view;
    }

    public boolean isInitialised() {
	return initialised;
    }

    protected void setInitialised(final boolean initialised) {
	this.initialised = initialised;
    }

    public Action getDeleteAction() {
	return deleteAction;
    }

    public void setBlockingLayer(final BlockingIndefiniteProgressLayer blockingLayer) {
	blockingLayerProvider.setBlockingLayer(blockingLayer);
    }

    /**
     * A convenient method for locking/unlocking blocking layer if it was provided. If the layer was not provided then nothing happens.
     * This method should be invoked on EDT.
     *
     * @param lock
     */
    protected void lockBlockingLayerIfProvided(final boolean lock) {
	if (blockingLayerProvider.getBlockingLayer() != null) {
	    blockingLayerProvider.getBlockingLayer().setLocked(lock);
	}
    }

    /**
     * A convenient method for setting a message text for the blocking layer if it was provided. If the layer was not provided then nothing happens.
     * This method should be invoked on EDT.
     *
     * @param lock
     */
    protected void setMessageForBlockingLayerIfProvided(final String msg) {
	if (blockingLayerProvider.getBlockingLayer() != null) {
	    blockingLayerProvider.getBlockingLayer().setText(msg);
	}
    }

}
