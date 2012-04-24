package ua.com.fielden.platform.swing.components.bind.development;

/*
 * Copyright (c) 2002-2008 JGoodies Karsten Lentzsch. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JToggleButton;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.IBindingEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.components.bind.development.Binder.EditableChangeListener;
import ua.com.fielden.platform.swing.components.bind.development.Binder.IPropertyConnector;
import ua.com.fielden.platform.swing.components.bind.development.Binder.IRebindable;
import ua.com.fielden.platform.swing.components.bind.development.Binder.IUpdatable;
import ua.com.fielden.platform.swing.components.bind.development.Binder.PropertyValidationResultsChangeListener;
import ua.com.fielden.platform.swing.components.bind.development.Binder.Rebinder;
import ua.com.fielden.platform.swing.components.bind.development.Binder.RequiredChangeListener;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.IOnCommitAction;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.IOnCommitActionable;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Converts ValueModels to the ToggleButtonModel interface. Useful to bind JToggleButton, JCheckBox and JCheckBoxMenuItem to a ValueModel.
 * <p>
 *
 * This adapter holds two values that represent the selected and the deselected state. These are used to determine the selection state if the underlying subject ValueModel changes
 * its value. If the selection is set, the corresponding representant is written to the underlying ValueModel.
 * <p>
 *
 * <strong>Constraints:</strong> The subject ValueModel must allow read-access to its value. Also, it is strongly recommended (though not required) that the underlying ValueModel
 * provides only two values, for example Boolean.TRUE and Boolean.FALSE. This is so because the toggle button component may behave "strangely" when it is used with ValueModels that
 * provide more than two elements.
 * <p>
 *
 * <strong>Examples:</strong>
 *
 * <pre>
 * // Recommended binding style using a factory
 * ValueModel model = presentationModel.getModel(MyBean.PROPERTYNAME_VISIBLE);
 * JCheckBox visibleBox = BasicComponentFactory.createCheckBox(model, &quot;Visible&quot;);
 * // Binding using the Bindings class
 * ValueModel model = presentationModel.getModel(MyBean.PROPERTYNAME_VISIBLE);
 * JCheckBox visibleBox = new JCheckBox(&quot;Visible&quot;);
 * Bindings.bind(visibleBox, model);
 * // Hand-made binding
 * ValueModel model = presentationModel.getModel(MyBean.PROPERTYNAME_VISIBLE);
 * JCheckBox visibleBox = new JCheckBox(&quot;Visible&quot;);
 * visibleBox.setModel(new ToggleButtonAdapter(model));
 * </pre>
 *
 * @author Karsten Lentzsch
 * @author TG Team
 * @version $Revision: 1.8 $
 *
 * @see javax.swing.ButtonModel
 * @see javax.swing.JCheckBox
 * @see javax.swing.JCheckBoxMenuItem
 */
public final class ToggleButtonAdapter extends JToggleButton.ToggleButtonModel implements IOnCommitActionable, IUpdatable, IRebindable, IPropertyConnector {
    private static final long serialVersionUID = 5373283565076651371L;

    /**
     * The value that represents the selected state.
     */
    private final Object selectedValue;

    /**
     * The value that represents the deselected state.
     */
    private final Object deselectedValue;

    private IBindingEntity entity;
    private final String propertyName;

    // entity specific listeners :
    private final SubjectValueChangeHandler subjectValueChangeHandler;
    private final PropertyValidationResultsChangeListener propertyValidationResultsChangeListener;
    private final EditableChangeListener editableChangeListener;
    private final RequiredChangeListener requiredChangeListener;

    /**
     * Top {@link HierarchicalPropertyChangeListener}
     */
    private HierarchicalPropertyChangeListener topListener;

    private final BoundedValidationLayer<? extends JToggleButton> boundedValidationLayer;

    private final List<IOnCommitAction> onCommitActions = new ArrayList<IOnCommitAction>();

    private final boolean readOnly;

    private boolean editable = true;

    private final JToggleButton toggleButton;

    // Instance Creation *****************************************************

    /**
     * Constructs a ToggleButtonAdapter on the given subject ValueModel. The created adapter will be selected if and only if the subject's initial value is
     * <code>Boolean.TRUE</code>.
     *
     * @param subject
     *            the subject that holds the value
     * @throws NullPointerException
     *             if the subject is {@code null}.
     */
    ToggleButtonAdapter(final IBindingEntity entity, final String propertyName, final BoundedValidationLayer<? extends JToggleButton> validationlayer, final boolean readOnly, final IOnCommitAction... actions) {
	this(entity, propertyName, Boolean.TRUE, Boolean.FALSE, validationlayer, readOnly, actions);
    }

    ToggleButtonAdapter(final BufferedPropertyWrapper bufferedPropertyWrapper, final BoundedValidationLayer<? extends JToggleButton> validationlayer, final boolean readOnly) {
	this(bufferedPropertyWrapper, Boolean.TRUE, Boolean.FALSE, validationlayer, readOnly);
    }

    /**
     * Constructs a ToggleButtonAdapter on the given subject ValueModel using the specified values for the selected and deselected state. The created adapter will be selected if
     * and only if the subject's initial value equals the given <code>selectedValue</code>.
     *
     * @param subject
     *            the subject that holds the value
     * @param selectedValue
     *            the value that will be set if this is selected
     * @param deselectedValue
     *            the value that will be set if this is deselected
     *
     * @throws NullPointerException
     *             if the subject is {@code null}.
     * @throws IllegalArgumentException
     *             if the selected and deselected values are equal
     */
    ToggleButtonAdapter(final IBindingEntity entity, final String propertyName, final Object selectedValue, final Object deselectedValue, final BoundedValidationLayer<? extends JToggleButton> boundedValidationLayer, final boolean readOnly, final IOnCommitAction... actions) {
	if (entity == null) {
	    throw new NullPointerException("The entity must not be null.");
	}
	if (propertyName == null) {
	    throw new NullPointerException("The propertyName must not be null.");
	}
	this.entity = entity;
	this.propertyName = propertyName;

	// initiate boundedValidationLayer
	if (boundedValidationLayer == null) {
	    throw new NullPointerException("The validationLayer must not be null.");
	}
	this.boundedValidationLayer = boundedValidationLayer;

	// initiateEditableComponent
	this.toggleButton = boundedValidationLayer.getView();
	if (toggleButton == null) {
	    throw new NullPointerException("The formatted field must not be null.");
	}

	// initiate Entity specific listeners
	this.subjectValueChangeHandler = !readOnly ? new SubjectValueChangeHandler() : null;
	this.propertyValidationResultsChangeListener = !readOnly ? new PropertyValidationResultsChangeListener(this.boundedValidationLayer) : null;
	this.editableChangeListener = !readOnly ? new EditableChangeListener(this.boundedValidationLayer) : null;
	this.requiredChangeListener = new RequiredChangeListener(this.boundedValidationLayer); // !readOnly ? new RequiredChangeListener(this.boundedValidationLayer) : null;

	// initiate and assign component specific listeners
	if (selectedValue.equals(deselectedValue)) {
	    throw new IllegalArgumentException("The selected value must not equal the deselected value.");
	}
	this.readOnly = readOnly;
	this.selectedValue = selectedValue;
	this.deselectedValue = deselectedValue;

	addOwnEntitySpecificListeners();
	Rebinder.initiateReconnectables(this.entity, this, this.boundedValidationLayer);

	for (int i = 0; i < actions.length; i++) {
	    addOnCommitAction(actions[i]);
	}

	// initial updating :
	this.updateStates();
	// setting OnCommitActionable
	this.initiateOnCommitActionable(boundedValidationLayer);
    }

    @Override
    public void initiateOnCommitActionable(final BoundedValidationLayer<?> boundedValidationLayer) {
	Rebinder.initiateIOnCommitActionable(this, boundedValidationLayer, entity);
    }

    @Override
    public void rebindTo(final IBindingEntity entity) {
	if (entity == null) {
	    new IllegalArgumentException("the component cannot be reconnected to the Null entity!!").printStackTrace();
	} else {
	    unbound();
	    setEntity(entity);
	    addOwnEntitySpecificListeners();
	    updateStates();
	}
    }

    @Override
    public void unbound() {
	removeOwnEntitySpecificListeners();
    }

    public IBindingEntity getSubjectBean() {
	return entity;
    }

    /**
     * Since rebinding is supported, the entity can be changed
     *
     * @param entity
     */
    protected void setEntity(final IBindingEntity entity) {
	this.entity = entity;
    }

    public void updateStates() {
	updateByActualOrLastIncorrectValue();
	if (boundedMetaProperty() != null) {
	    updateEditable();
	    updateRequired();
	}
	updateToolTip();
	if (boundedMetaProperty() != null) {
	    updateValidationResult();
	}
    }

    public void addOwnEntitySpecificListeners() {
	if (!readOnly) {
	    Rebinder.addPropertySpecificListener(this.entity, this.propertyName, this.subjectValueChangeHandler);
	    Rebinder.addMetaPropertySpecificListeners(this.entity, this.propertyName, this.propertyValidationResultsChangeListener, this.editableChangeListener, this.requiredChangeListener);
	} else {
	    topListener = HierarchicalPropertyChangeListener.addListenersToPropertyHierarchy(Rebinder.getActualEntity(entity), propertyName, new SubjectValueChangeHandler(), new PropertyValidationResultsChangeListener(this.boundedValidationLayer));
	    if (!this.propertyName.contains(".")) {
		Rebinder.addMetaPropertySpecificListeners(this.entity, this.propertyName, null, null, this.requiredChangeListener);
	    }
	}
    }

    public void removeOwnEntitySpecificListeners() {
	if (!readOnly) {
	    Rebinder.removePropertySpecificListener(this.entity, this.propertyName, this.subjectValueChangeHandler);
	    Rebinder.removeMetaPropertySpecificListeners(this.entity, this.propertyName, this.propertyValidationResultsChangeListener, this.editableChangeListener, this.requiredChangeListener);
	} else {
	    if (!this.propertyName.contains(".")) {
		Rebinder.removeMetaPropertySpecificListeners(this.entity, this.propertyName, null, null, this.requiredChangeListener);
	    }
	    HierarchicalPropertyChangeListener.removeListenersFromHierarchy(topListener);
	    topListener = null;
	}
    }

    public ToggleButtonAdapter(final BufferedPropertyWrapper bufferedPropertyWrapper, final Object selectedValue, final Object deselectedValue, final BoundedValidationLayer<? extends JToggleButton> boundedValidationlayer, final boolean readOnly) {
	this(bufferedPropertyWrapper, bufferedPropertyWrapper.getPropertyName(), selectedValue, deselectedValue, boundedValidationlayer, readOnly);
    }

    @Override
    public boolean isOnKeyTyped() {
	return !(entity instanceof BufferedPropertyWrapper) && (!(entity instanceof AutocompleterBufferedPropertyWrapper));
    }

    // ToggleButtonModel Implementation ***********************************

    /**
     * First, the subject value is set to this adapter's selected value if the argument is {@code true}, to the deselected value otherwise. Second, this adapter's state is set to
     * the then current subject value. This ensures that the selected state is synchronized with the subject - even if the subject rejects the change.
     *
     * @param b
     *            {@code true} sets the selected value as subject value, {@code false} sets the deselected value as subject value
     */
    @Override
    public void setSelected(final boolean b) {
	if (!readOnly && editable) {
	    if (isOnKeyTyped()) {
		// lock if the "entity" is not BPW. if "entity" is BPW - it locks inside BPW's "commit" method
		// lock subject bean, even if the setter will not be perfomed (it is more safe)
		entity.lock();
	    }
	    new SwingWorkerCatcher<Result, Void>() {
		private boolean setterPerformed = false;

		@Override
		protected Result tryToDoInBackground() {
		    // if (!BindingUtils.equals(b ? selectedValue : deselectedValue, entity.get(propertyName))) {
		    entity.set(propertyName, b ? selectedValue : deselectedValue);
		    setterPerformed = true;
		    // }
		    return null;
		}

		@Override
		protected void tryToDone() {
		    if (boundedMetaProperty() != null && boundedMetaProperty().isValid()) {
			// if the property passed validation -> update its value by the actual value
			final boolean subjectHoldsChoiceValue = selectedValue.equals(entity.get(propertyName));
			ToggleButtonAdapter.super.setSelected(subjectHoldsChoiceValue);
		    } else {
			// if validation didn't pass -> set UI state the incorrect value
			ToggleButtonAdapter.super.setSelected(b);
		    }
		    if (setterPerformed) {
			for (int i = 0; i < onCommitActions.size(); i++) {
			    if (onCommitActions.get(i) != null) {
				onCommitActions.get(i).postCommitAction();
				if (boundedMetaProperty() == null || boundedMetaProperty().isValid()) {
				    onCommitActions.get(i).postSuccessfulCommitAction();
				} else {
				    onCommitActions.get(i).postNotSuccessfulCommitAction();
				}
			    }
			}
		    }
		    if (isOnKeyTyped()) {
			// need to unlock subjectBean in all cases:
			// 1. setter not performed - exception throwed
			// 2. setter not performed - the committing logic didn't invoke setter
			// 3. setter performed correctly
			entity.unlock();
		    }
		}
	    }.execute();
	}

    }

    // Event Handling *********************************************************

    /**
     * Handles changes in the subject's value.
     */
    private final class SubjectValueChangeHandler implements Binder.SubjectValueChangeHandler {

	/**
	 * The subject value has changed. Updates this adapter's selected state to reflect whether the subject holds the selected value or not.
	 *
	 * @param evt
	 *            the property change event fired by the subject
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
	    updateByActualOrLastIncorrectValue();
	    updateToolTip();
	}

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

    /**
     * updates the editable state of the component based on the Editable state of the bound Property
     */
    public void updateEditable() {
	final MetaProperty property = boundedMetaProperty();
	if (property != null) {
	    SwingUtilitiesEx.invokeLater(new Runnable() {
		@Override
		public void run() {
		    boundedValidationLayer.getView().setEnabled(property.isEditable());
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

    @Override
    public final MetaProperty boundedMetaProperty() {
	return EntityUtils.findFirstFailedMetaProperty((AbstractEntity<?>) Rebinder.getActualEntity(entity), propertyName);
    }

    @Override
    public void updateByActualOrLastIncorrectValue() {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    public void run() {
		final MetaProperty metaProperty = boundedMetaProperty();
		Object value;
		if (metaProperty == null || metaProperty.isValid()) {
		    value = entity.get(propertyName);
		} else if (metaProperty != null && !metaProperty.isValid()) {
		    value = metaProperty.getLastInvalidValue();
		} else {
		    value = deselectedValue;
		}

		final boolean subjectHoldsChoiceValue = selectedValue.equals(value);
		ToggleButtonAdapter.super.setSelected(subjectHoldsChoiceValue);
	    }
	});
    }

    @Override
    public void updateToolTip() {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    public void run() {
		boundedValidationLayer.getView().setToolTipText(Binder.createToolTipByValueAndMetaProperty(entity, propertyName, boundedMetaProperty(), boundedValidationLayer.getOriginalToolTipText(), false));
	    }
	});
    }

    @Override
    public void updateValidationResult() {
	Binder.updateValidationUIbyMetaPropertyValidationState(boundedMetaProperty(), boundedValidationLayer);
    }

    public boolean isEditable() {
	return editable;
    }

    public void setEditable(final boolean editable) {
	this.editable = editable;
    }

    @Override
    public String getPropertyName() {
	return propertyName;
    }
}