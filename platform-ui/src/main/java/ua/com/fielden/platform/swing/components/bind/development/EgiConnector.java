package ua.com.fielden.platform.swing.components.bind.development;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IBindingEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.swing.components.bind.development.Binder.EditableChangeListener;
import ua.com.fielden.platform.swing.components.bind.development.Binder.IRebindable;
import ua.com.fielden.platform.swing.components.bind.development.Binder.PropertyConnectorAdapter;
import ua.com.fielden.platform.swing.components.bind.development.Binder.PropertyValidationResultsChangeListener;
import ua.com.fielden.platform.swing.components.bind.development.Binder.Rebinder;
import ua.com.fielden.platform.swing.components.bind.development.Binder.RequiredChangeListener;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.IOnCommitAction;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.IOnCommitActionable;
import ua.com.fielden.platform.swing.egi.EntityGridInspector;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

public class EgiConnector<T extends AbstractEntity> extends PropertyConnectorAdapter implements IOnCommitActionable, IRebindable {

    private final EntityGridInspector<T> entityGridInspector;

    private final BoundedValidationLayer<? extends EntityGridInspector<T>> boundedValidationLayer;

    private final List<IOnCommitAction> onCommitActions = new ArrayList<IOnCommitAction>();

    @SuppressWarnings("unchecked")
    EgiConnector(final IBindingEntity entity, final String propertyName, final BoundedValidationLayer<? extends EntityGridInspector<T>> boundedValidationLayer, final IOnCommitAction... actions) {
	// initiate Entity and PropertyName
	super(entity, propertyName);

	// initiate boundedValidationLayer
	if (boundedValidationLayer == null) {
	    throw new NullPointerException("The validationLayer must not be null.");
	}
	this.boundedValidationLayer = boundedValidationLayer;

	// initiateEditableComponent
	this.entityGridInspector = this.boundedValidationLayer.getView();
	if (entityGridInspector == null) {
	    throw new NullPointerException("The gridInspector must not be null.");
	}
	entityGridInspector.getActualModel().setInstances((List<T>) entity.get(propertyName));

	// initiate Entity specific listeners
	this.subjectValueChangeHandler = new SubjectValueChangeHandler();
	this.propertyValidationResultsChangeListener = new PropertyValidationResultsChangeListener(this.boundedValidationLayer);
	this.editableChangeListener = new EditableChangeListener(this.boundedValidationLayer);
	this.requiredChangeListener = new RequiredChangeListener(this.boundedValidationLayer);

	addOwnEntitySpecificListeners();
	Rebinder.initiateReconnectables(this.entity, this, this.boundedValidationLayer);

	// initiate and assign component specific listeners
	// ==================  add component specific listeners :
	// TODO : add component specific listeners (after the support for remove/add/set operations will be added to EGI)

	// add on Commit Actions
	for (int i = 0; i < actions.length; i++) {
	    addOnCommitAction(actions[i]);
	}

	// initial updating :
	this.updateStates();
	// setting OnCommitActionable
	this.initiateOnCommitActionable(boundedValidationLayer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void rebindTo(final IBindingEntity entity) {
	if (entity == null) {
	    new IllegalArgumentException("the component cannot be reconnected to the Null entity!!").printStackTrace();
	} else {
	    unbound();
	    setEntity(entity);
	    addOwnEntitySpecificListeners();
	    entityGridInspector.getActualModel().setInstances((List<T>) this.entity.get(propertyName));

	    //updateByActualOrLastIncorrectValue(); - update is invoked in setInstances method
	    updateEditable();
	    updateToolTip();
	    updateValidationResult();
	}
    }

    @Override
    public void unbound() {
	removeOwnEntitySpecificListeners();
    }

    /**
     * adds OnCommitAction to use it at On Key Typed commit model
     *
     * @param onCommitAction
     * @return
     */
    public synchronized boolean addOnCommitAction(final IOnCommitAction onCommitAction) {
	return onCommitActions.add(onCommitAction);
    }

    /**
     * removes OnCommitAction to remove its usage at On Key Typed commit model
     *
     * @param onCommitAction
     * @return
     */
    public synchronized boolean removeOnCommitAction(final IOnCommitAction onCommitAction) {
	return onCommitActions.remove(onCommitAction);
    }

    /**
     * gets all assigned "On Key Typed" OnCommitActions
     *
     * @return
     */
    public List<IOnCommitAction> getOnCommitActions() {
	return Collections.unmodifiableList(onCommitActions);
    }

    private final class SubjectValueChangeHandler implements Binder.SubjectValueChangeHandler {

	/**
	 * The subject value has changed; updates the document immediately or later - depending on the <code>updateLater</code> state.
	 *
	 * @param evt
	 *            the event to handle
	 * @throws MissingConverterException
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void propertyChange(final PropertyChangeEvent evt) {
	    SwingUtilitiesEx.invokeLater(new Runnable() {
		public void run() {
		    if (boundedMetaProperty() == null || boundedMetaProperty().isValid()) {
			final Object oldValue = evt.getOldValue(), newValue = evt.getNewValue();
			if (oldValue instanceof Integer && newValue instanceof Integer) { // Mutator == SETTER
			    entityGridInspector.getActualModel().setInstances((List<T>) entity.get(propertyName));
			} else if (oldValue instanceof Integer) { // Mutator == INCREMENTOR
			    entityGridInspector.getActualModel().addInstances((T) newValue);
			} else { // Mutator == DECREMENTOR
			    entityGridInspector.getActualModel().removeInstances((T) oldValue);
			}
		    }
		    updateToolTip();
		}
	    });
	}

    }

    public void updateByActualOrLastIncorrectValue() {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    public void run() {
		entityGridInspector.getActualModel().fireTableDataChanged();
	    }
	});
    }

    //    @Override
    //    public void updateToolTip() {
    //	SwingUtilitiesEx.invokeLater(new Runnable() {
    //	    public void run() {
    //		if (boundedMetaProperty() == null){
    //		    boundedValidationLayer.getView().setToolTipText(boundedValidationLayer.getOriginalToolTipText());
    //		    return;
    //		}
    //		boundedValidationLayer.getView().setToolTipText((boundedMetaProperty().isValid()) ? (boundedMetaProperty().hasWarnings() ? boundedMetaProperty().getFirstWarning().getMessage()
    //			: boundedValidationLayer.getOriginalToolTipText())
    //			: boundedMetaProperty().getFirstFailure().getMessage());
    //	    }
    //	});
    //    }


    @Override
    public void updateToolTip() {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    public void run() {
		boundedValidationLayer.getView().setToolTipText(Binder.createToolTipByValueAndMetaProperty(entity, propertyName, boundedMetaProperty(), boundedValidationLayer.getOriginalToolTipText(), true));
	    }
	});
    }

    @Override
    public void updateValidationResult() {
	Binder.updateValidationUIbyMetaPropertyValidationState(boundedMetaProperty(), this.boundedValidationLayer);
    }

    /**
     * updates the editable state of the component based on the Editable state of the bound Property
     */
    public void updateEditable() {
	final MetaProperty property = boundedMetaProperty();
	if (property != null) {
	    SwingUtilitiesEx.invokeLater(new Runnable() {
		@Override
		public void run() {
		    entityGridInspector.setEnabled(property.isEditable());
		}
	    });
	}
    }

    /**
     * updates the "required" state of the component based on the "required" state of the bound Property
     */
    public void updateRequired() {
	final MetaProperty property = boundedMetaProperty();
	if (property != null) {
	    SwingUtilitiesEx.invokeLater(new Runnable() {
		@Override
		public void run() {
		    boundedValidationLayer.getUI().setRequired(property.isRequired());
		}
	    });
	}
    }

}
