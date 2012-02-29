package ua.com.fielden.platform.swing.ei;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.ei.editors.IPropertyBinder;

/**
 * A model for {@link EntityInspector}.
 * 
 * Provides three actions -- save, cancel (reload), delete.
 * <p>
 * <p>
 * <b>IMPORTANT:</b> EI has a mutable state, which is an entity instance represented by EI. The originally passed into the EI constructor reference to entity instance gets changed.
 * </p>
 * <p>
 * 
 * TODO provide support for post action events such as afterSave, afterCancel etc.
 * 
 * @author 01es
 * 
 */
@SuppressWarnings("rawtypes")
public class CrudEntityInspectorModel<T extends AbstractEntity> extends EntityInspectorModel<T> {

    private final IEntityDao<T> dao;

    private final Action save;
    private final Action cancel;
    private final Action delete;
    private final IAfterActions afterActions;

    /**
     * EI model constructor.
     * 
     * @param entity
     *            -- an entity instance that is being represented by EI, which can be a brand new instance not yet persisted.
     * @param dao
     *            -- defines how an entity instance can be saved, reloaded and deleted.
     * @param valueMatcherFactory
     *            -- required to obtain value matchers for autocompleter-based editors used for properties of entity types.
     */
    public CrudEntityInspectorModel(final T entity, final IPropertyBinder binder, final IEntityDao<T> dao, final IAfterActions afterActions) {
	super(entity, binder);
	this.dao = dao;
	this.afterActions = afterActions;

	save = createSaveAction();
	cancel = createCancelAction();
	delete = createDeleteAction();
    }

    /**
     * Similar to the above, but with no {@link IAfterActions} instance.
     * 
     * @param entity
     * @param binder
     * @param dao
     */
    public CrudEntityInspectorModel(final T entity, final IPropertyBinder binder, final IEntityDao<T> dao) {
	this(entity, binder, dao, null);
    }

    private Action createSaveAction() {
	final Action action = new Command<Result>("Save") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected Result action(final ActionEvent e) throws Exception {
		try {
		    final Result validationResult = getEntity().isValid();
		    if (!validationResult.isSuccessful()) {
			return validationResult;
		    }
		    dao.save(getEntity());
		    return new Result(getEntity(), "Saved successfully.");
		} catch (final RuntimeException ex) {
		    return new Result(getEntity(), ex);
		}
	    }

	    @Override
	    protected void postAction(final Result result) {
		if (!result.isSuccessful()) {
		    JOptionPane.showMessageDialog(null, result.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
		} else {
		    if (CrudEntityInspectorModel.this.afterActions != null) {
			CrudEntityInspectorModel.this.afterActions.afterSave();
		    }
		}
		super.postAction(result);
	    }
	};
	action.setEnabled(true);
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
	action.putValue(Action.SHORT_DESCRIPTION, "Save changes");
	return action;
    }

    private Action createCancelAction() {
	final Action action = new Command<T>("Cancel") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected T action(final ActionEvent e) throws Exception {
		try {
		    if (dao.entityExists(getEntity())) {
			final T refreshedEntity = dao.findById(getEntity().getId());
			return refreshedEntity;
		    }
		    return null;
		} catch (final RuntimeException ex) {
		    return null;
		}
	    }

	    @Override
	    protected void postAction(final T entity) {
		if (entity == null) {
		    JOptionPane.showMessageDialog(null, "Cannot cancel changes for a new entity.", "Cancel error", JOptionPane.ERROR_MESSAGE);
		} else {
		    setEntity(entity);
		    if (CrudEntityInspectorModel.this.afterActions != null) {
			CrudEntityInspectorModel.this.afterActions.afterCancel();
		    }
		}
		super.postAction(entity);
	    }
	};
	action.setEnabled(true);
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
	action.putValue(Action.SHORT_DESCRIPTION, "Cancel changes -- reloads the entity");
	return action;
    }

    private Action createDeleteAction() {
	final Action action = new Command<Result>("Delete") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected Result action(final ActionEvent e) throws Exception {
		return new Result(getEntity(), new Exception("Deletion is not yet supported."));
	    }

	    @Override
	    protected void postAction(final Result result) {
		if (!result.isSuccessful()) {
		    JOptionPane.showMessageDialog(null, result.getMessage(), "Delete warning", JOptionPane.WARNING_MESSAGE);
		} else {
		    if (CrudEntityInspectorModel.this.afterActions != null) {
			CrudEntityInspectorModel.this.afterActions.afterDelete();
		    }
		}
		super.postAction(result);
	    }
	};
	action.setEnabled(true);
	action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);
	action.putValue(Action.SHORT_DESCRIPTION, "Delete entity");
	return action;
    }

    /**
     * Represents the actions that would invoke after SUCCESSFUL save/cancel/delete action.
     * 
     * @author Jhou
     * 
     */
    public interface IAfterActions {
	/**
	 * The action invoked after SUCCESSFUL save action
	 */
	void afterSave();

	/**
	 * The action invoked after SUCCESSFUL cancel action
	 */
	void afterCancel();

	/**
	 * The action invoked after SUCCESSFUL delete action
	 */
	void afterDelete();
    }

    public IEntityDao<T> getDao() {
	return dao;
    }

    public Action getSave() {
	return save;
    }

    public Action getCancel() {
	return cancel;
    }

    public Action getDelete() {
	return delete;
    }

}
