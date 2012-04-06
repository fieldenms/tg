package ua.com.fielden.platform.swing.model;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JOptionPane;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.NotificationLayer.MessageType;
import ua.com.fielden.platform.swing.components.bind.development.Binder;
import ua.com.fielden.platform.swing.dialogs.DialogWithDetails;
import ua.com.fielden.platform.swing.ei.editors.development.ILightweightPropertyBinder;
import ua.com.fielden.platform.swing.ei.editors.development.IPropertyEditor;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.swing.view.IEntityMasterCache;

/**
 * This is a convenient UI model based on {@link UmMaster} that supports basic CRUD actions -- new, save, edit, cancel (no delete at this stage).
 * <p>
 * A special place in this class have two methods:
 * <ul>
 * <li>{@link #notifyActionStageChange(ua.com.fielden.platform.swing.model.UModel.ActionStage)} -- should be implemented to react to CRUD action events;
 * <li>{@link #newEntity(EntityFactory)} -- instantiate new entity in response to action <i>new</i>; should be overridden if an alternative implementation is required.
 * </ul>
 *
 * @author TG Team
 *
 * @param <T>
 *            -- entity type.
 * @param <C>
 *            -- controller type.
 */
public abstract class UmMasterWithCrud<T extends AbstractEntity<?>, C extends IEntityDao<T>> extends UmMaster<T, C> {

    /** Is used in situation where user invokes action new and then cancel */
    private T prevEntity;
    /** Cache instance, which manages all master frames. */
    private final IEntityMasterCache cache;
    /** Responsible for creation of new entity instances */
    private final IEntityProducer<T> entityProducer;

    protected UmMasterWithCrud(final IEntityProducer<T> entityProducer, final IEntityMasterCache cache, final T entity, final C controller, final ILightweightPropertyBinder<T> propertyBinder, final fetch<T> fm, final boolean lazy) {
	super(entity, controller, propertyBinder, fm, lazy);
	this.cache = cache;
	this.entityProducer = entityProducer;
    }

    /** A convenient method for retrieving managed entity by id. */
    @Override
    protected T findById(final Long id, final boolean forceRetrieval) {
	if (id == null) {
	    return getEntity();
	}

	final T entity = getEntity().getId() == id ? getEntity() : getPrevEntity();

	if (forceRetrieval) {
	    return getFetchModel() != null ? getController().findById(id, getFetchModel()) : getController().findById(id);
	} else if (getController().isStale(id, entity.getVersion())) {
	    return getFetchModel() != null ? getController().findById(id, getFetchModel()) : getController().findById(id);
	} else {
	    return entity;
	}
    }

    @Override
    protected Map<String, IPropertyEditor> buildEditors(final T entity, final C controller, final ILightweightPropertyBinder<T> propertyBinder) {
	return propertyBinder.bind(entity);
    }

    /**
     * Updates a cache of master frames by putting an associated frame with this model's view into cache with entitie's id. Should be invoked on EDT.
     */
    protected void updateCache(final T entity) {
	if (cache != null) {
	    // need to update the cache only if this model's view is on a frame and the entity is persisted
	    if (entity.isPersisted() && getView() != null && getView().getFrame() != null) {
		try {
		    cache.put(getView().getFrame(), entity.getId()); // could throw an exception if such association already exists.
		} catch (final IllegalStateException ex) {
		    //
		    if (JOptionPane.showConfirmDialog(getView().getFrame(), "There is another master open for the same instance.\n" + "Would you like to activate it?", "Warning", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			cache.get(entity.getId()).setVisible(true);
			final ICloseGuard guard = getView().getFrame().canClose();
			if (guard == null) {
			    getView().getFrame().close();
			} else {
			    notify(guard, getView().getFrame());
			}

		    } else {
			if (getEntityProducer() == null) {
			    throw new Result(null, new IllegalStateException("Entity producer was not provided."));
			}
			setEntity(getEntityProducer().newEntity());
			setState(UmState.VIEW);
		    }
		}
	    }
	}
    }

    private void notify(final ICloseGuard guard, final BaseFrame frame) {
	JOptionPane.showMessageDialog(frame, guard.whyCannotClose(), "Warning", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * This method is invoked as part of the save action in order to update any dependent models representing details of the master/details relationships. By default it does
     * nothing, and thus should be overridden whenever is necessary. The method is executed on EDT, which should not be a problem since updating means only setting the master
     * entity and rebinding of editors, which should anyway happen on EDT.
     * */
    protected void updateDetailsModels() {
    }

    @Override
    protected Action createCancelAction() {
	final Command<T> action = new Command<T>("Cancel") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		notifyActionStageChange(ActionStage.CANCEL_PRE_ACTION);
		setState(UmState.UNDEFINED);
		getSaveAction().setEnabled(false);
		return super.preAction();
	    }

	    @Override
	    protected T action(final ActionEvent arg0) throws Exception {
		notifyActionStageChange(ActionStage.CANCEL_ACTION);
		// wait before the commit process finish
		try {
		    if (!getManagedEntity().isPersisted() && getPrevEntity() != null && getPrevEntity().isPersisted()) {
			return getPrevEntity();
		    } else if (getManagedEntity().isPersisted()) {
			return (T) getManagedEntity().restoreToOriginal();
		    }
		    return (T) getManagedEntity().restoreToOriginal();
		} finally {
		    setPrevEntity(null);
		}
	    }

	    @Override
	    protected void postAction(final T entity) {
		if (getView() != null) {
		    getView().notify("", MessageType.NONE);
		}
		setEntity(entity);
		setState(UmState.VIEW);
		updateCache(entity);
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
	final Command<T> action = new Command<T>("Edit") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		notifyActionStageChange(ActionStage.EDIT_PRE_ACTION);
		setState(UmState.UNDEFINED);
		return super.preAction();
	    }

	    @Override
	    protected T action(final ActionEvent arg0) throws Exception {
		try {
		    return getManagedEntity().isPersisted()//
		    ? findById(getManagedEntity().getId(), false)
			    : getManagedEntity();
		} finally {
		    notifyActionStageChange(ActionStage.EDIT_ACTION);
		}
	    }

	    @Override
	    protected void postAction(final T entity) {
		setEntity(entity);
		setState(UmState.EDIT);
		notifyActionStageChange(ActionStage.EDIT_POST_ACTION);
	    }

	};
	action.setEnabled(true);
	action.putValue(Action.SHORT_DESCRIPTION, "Edit");
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
	return action;
    }

    @Override
    protected Action createNewAction() {
	final Command<T> action = new Command<T>("New") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		notifyActionStageChange(ActionStage.NEW_PRE_ACTION);
		setState(UmState.UNDEFINED);
		return super.preAction();
	    }

	    @Override
	    protected T action(final ActionEvent event) throws Exception {
		notifyActionStageChange(ActionStage.NEW_ACTION);
		setPrevEntity(getManagedEntity());
		if (getEntityProducer() == null) {
		    throw new Result(null, new IllegalStateException("Entity producer was not provided."));
		}
		return getEntityProducer().newEntity();
	    }

	    @Override
	    protected void postAction(final T newEntity) {
		// remove model's view frame from cache in case where previous entity was persisted
		if (getPrevEntity() != null && getPrevEntity().isPersisted()) {
		    cache.remove(getPrevEntity().getId());
		}
		setEntity(newEntity);
		setState(UmState.NEW);
		notifyActionStageChange(ActionStage.NEW_POST_ACTION);
	    }

	};
	action.setEnabled(true);
	action.putValue(Action.SHORT_DESCRIPTION, "Create new");
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
	return action;
    }

    @Override
    protected Action createRefreshAction() {
	final Command<T> action = new Command<T>("Refresh") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		notifyActionStageChange(ActionStage.REFRESH_PRE_ACTION);
		setState(UmState.UNDEFINED);
		Binder.commitFocusOwner();
		return super.preAction();
	    }

	    @Override
	    protected T action(final ActionEvent event) throws Exception {
		try {
		    return getManagedEntity().isPersisted()//
		    ? findById(getManagedEntity().getId(), true)
			    : getManagedEntity();
		} catch(final Exception ex) {
		    setState(UmState.VIEW);
		    throw ex;
		} finally {
		    notifyActionStageChange(ActionStage.REFRESH_ACTION);
		}
	    }

	    @Override
	    protected void postAction(final T entity) {
		setEntity(entity);
		setState(UmState.VIEW);
		updateDetailsModels();
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
		getCancelAction().setEnabled(false);

		lockBlockingLayerIfProvided(true);
		setMessageForBlockingLayerIfProvided("Saving...");

		return super.preAction();
	    }

	    @Override
	    protected Result action(final ActionEvent arg0) throws Exception {
		notifyActionStageChange(ActionStage.SAVE_ACTION);
		try {
		    // wait before the commit process finish
		    final Result result = getManagedEntity().isValid();
		    if (result.isSuccessful()) {
			setEntity(getController().save(getManagedEntity()));
		    }
		    return result;
		} catch (final Exception ex) {
		    return new Result(getManagedEntity(), ex);
		}
	    }

	    @Override
	    protected void postAction(final Result result) {
		try {
		    if (result.isSuccessful()) {
			if (getView() != null) {
			    getView().notify("", MessageType.NONE);
			}
			setPrevEntity(getEntity());
			updateCache(getEntity());
			setState(UmState.VIEW);
			updateDetailsModels();
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
		} finally {
		    lockBlockingLayerIfProvided(false);
		}
	    }

	};
	action.setEnabled(true);
	action.putValue(Action.SHORT_DESCRIPTION, "Save changes");
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
	return action;
    }

    @Override
    protected Action createDeleteAction() {
	final Command<Void> action = new Command<Void>("Delete") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		notifyActionStageChange(ActionStage.DELETE_PRE_ACTION);
		setState(UmState.UNDEFINED);
		Binder.commitFocusOwner();
		return super.preAction();
	    }

	    @Override
	    protected Void action(final ActionEvent event) throws Exception {
		try {
		    if (getManagedEntity().isPersisted()) {
			getController().delete(getManagedEntity());
		    }
		} finally {
		    notifyActionStageChange(ActionStage.DELETE_ACTION);
		}
		return null;
	    }

	    @Override
	    protected void postAction(final Void entity) {
		setState(UmState.VIEW);
		notifyActionStageChange(ActionStage.DELETE_POST_ACTION);
	    }

	};
	action.setEnabled(true);
	action.putValue(Action.SHORT_DESCRIPTION, "Delete entity");
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);
	return action;
    }

    protected T getPrevEntity() {
	return prevEntity;
    }

    protected void setPrevEntity(final T prevEntity) {
	this.prevEntity = prevEntity;
    }

    public IEntityMasterCache getCache() {
	return cache;
    }

    public IEntityProducer<T> getEntityProducer() {
	return entityProducer;
    }

}
