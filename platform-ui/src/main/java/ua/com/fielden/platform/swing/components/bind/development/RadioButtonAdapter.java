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

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

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
import ua.com.fielden.platform.utils.PropertyChangeSupportEx.CheckingStrategy;

/**
 * Converts ValueModels to the ToggleButtonModel interface. Useful to bind JRadioButtons and JRadioButtonMenuItems to a ValueModel.
 * <p>
 *
 * This adapter holds a <em>choice</em> object that is used to determine the selection state if the underlying subject ValueModel changes its value. This model is selected if the
 * subject's value equals the choice object. And if the selection is set, the choice object is set to the subject.
 * <p>
 *
 * <strong>Note:</strong> You must not use a ButtonGroup with this adapter. The RadioButtonAdapter ensures that only one choice is selected by sharing a single subject ValueModel -
 * at least if all choice values differ. See also the example below.
 * <p>
 *
 * <strong>Example:</strong>
 *
 * <pre>
 * // Recommended binding style using a factory
 * PresentationModel presentationModel = new PresentationModel(printerSettings);
 * ValueModel orientationModel =
 *     presentationModel.getModel(PrinterSettings.PROPERTYNAME_ORIENTATION);
 * JRadioButton landscapeButton = BasicComponentFactory.createRadioButton(
 *     orientationModel, PrinterSettings.LANDSCAPE, &quot;Landscape&quot;);
 * JRadioButton portraitButton  = BasicComponentFactory.createRadioButton(
 *     orientationModel, PrinterSettings.PORTRAIT, &quot;Portrait&quot;);
 * // Binding using the Bindings class
 * ValueModel orientationModel =
 *     presentationModel.getModel(PrinterSettings.PROPERTYNAME_ORIENTATION);
 * JRadioButton landscapeButton = new JRadioButton(&quot;Landscape&quot;);
 * Bindings.bind(landscapeButton, orientationModel, &quot;landscape&quot;);
 * JRadioButton portraitButton = new JRadioButton(&quot;Portrait&quot;);
 * Bindings.bind(portraitButton, orientationModel, &quot;portrait&quot;);
 * // Hand-made style
 * ValueModel orientationModel =
 *     presentationModel.getModel(PrinterSettings.PROPERTYNAME_ORIENTATION);
 * JRadioButton landscapeButton = new JRadioButton(&quot;Landscape&quot;);
 * landscapeButton.setModel(new RadioButtonAdapter(model, &quot;landscape&quot;);
 * JRadioButton portraitButton = new JRadioButton(&quot;Portrait&quot;);
 * portraitButton.setModel(new RadioButtonAdapter(model, &quot;portrait&quot;);
 * </pre>
 *
 * @author Karsten Lentzsch
 * @version $Revision: 1.7 $
 *
 * @see javax.swing.ButtonModel
 * @see javax.swing.JRadioButton
 * @see javax.swing.JRadioButtonMenuItem
 */
@SuppressWarnings("unchecked")
public final class RadioButtonAdapter extends JToggleButton.ToggleButtonModel implements IOnCommitActionable, IUpdatable, IRebindable, IPropertyConnector {
    private static final long serialVersionUID = -1898279366855570355L;

    private IBindingEntity entity;
    private final String propertyName;

    // entity specific listeners :
    private final SubjectValueChangeHandler subjectValueChangeHandler;
    private final PropertyValidationResultsChangeListener propertyValidationResultsChangeListener;
    private final EditableChangeListener editableChangeListener;
    private final RequiredChangeListener requiredChangeListener;

    /**
     * Holds the object that is compared with the subject's value to determine whether this adapter is selected or not.
     */
    private final Object choice;

    private final BoundedValidationLayer<JRadioButton> boundedValidationLayer;

    private final List<IOnCommitAction> onCommitActions = new ArrayList<IOnCommitAction>();

    // Instance Creation ****************************************************

    public IBindingEntity getSubjectBean() {
	return entity;
    }

    /**
     * Constructs a RadioButtonAdapter on the given subject ValueModel for the specified choice. The created adapter will be selected if and only if the subject's initial value
     * equals the given <code>choice</code>.
     *
     * @param subject
     *            the subject that holds the value
     * @param choice
     *            the choice that indicates that this adapter is selected
     *
     * @throws NullPointerException
     *             if the subject is {@code null}
     */
    RadioButtonAdapter(final IBindingEntity entity, final String propertyName, final Object choice, final BoundedValidationLayer<JRadioButton> boundedValidationLayer, final IOnCommitAction... actions) {
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
	if (boundedValidationLayer.getView() == null) {
	    throw new NullPointerException("The formatted field must not be null.");
	}

	// initiate Entity specific listeners
	this.subjectValueChangeHandler = new SubjectValueChangeHandler();
	this.propertyValidationResultsChangeListener = new PropertyValidationResultsChangeListener(this.boundedValidationLayer);
	this.editableChangeListener = new EditableChangeListener(this.boundedValidationLayer);
	this.requiredChangeListener = new RequiredChangeListener(this.boundedValidationLayer);

	addOwnEntitySpecificListeners();
	Rebinder.initiateReconnectables(this.entity, this, this.boundedValidationLayer);

	// initiate and assign component specific listeners
	this.choice = choice;
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

    RadioButtonAdapter(final BufferedPropertyWrapper bufferedPropertyWrapper, final Object choice, final BoundedValidationLayer<JRadioButton> boundedValidationLayer) {
	this(bufferedPropertyWrapper, bufferedPropertyWrapper.getPropertyName(), choice, boundedValidationLayer);
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
	Rebinder.addPropertySpecificListener(this.entity, this.propertyName, this.subjectValueChangeHandler);
	Rebinder.addMetaPropertySpecificListeners(this.entity, this.propertyName, this.propertyValidationResultsChangeListener, this.editableChangeListener, this.requiredChangeListener);
    }

    public void removeOwnEntitySpecificListeners() {
	Rebinder.removePropertySpecificListener(this.entity, this.propertyName, this.subjectValueChangeHandler);
	Rebinder.removeMetaPropertySpecificListeners(this.entity, this.propertyName, this.propertyValidationResultsChangeListener, this.editableChangeListener, this.requiredChangeListener);
    }

    @Override
    public final MetaProperty boundedMetaProperty() {
	return Rebinder.getActualEntity(entity).getProperty(propertyName);
    }

    @Override
    public boolean isOnKeyTyped() {
	return !(entity instanceof BufferedPropertyWrapper) && (!(entity instanceof AutocompleterBufferedPropertyWrapper));
    }

    // ToggleButtonModel Implementation ***********************************

    /**
     * First, the subject value is set to this adapter's choice value if the argument is {@code true}. Second, this adapter's state is set to the then current subject value. The
     * latter ensures that the selection state is synchronized with the subject - even if the subject rejects the change.
     * <p>
     *
     * Does nothing if the boolean argument is {@code false}, or if this adapter is already selected.
     *
     * @param b
     *            {@code true} sets the choice value as subject value, and is intended to select this adapter (although it may not happen); {@code false} does nothing
     */
    @Override
    public void setSelected(final boolean b) {
	if (isOnKeyTyped()) {
	    // lock if the "entity" is not BPW. if "entity" is BPW - it locks inside BPW's "commit" method
	    // lock subject bean, even if the setter will not be perfomed (it is more safe)
	    entity.lock();
	}
	new SwingWorkerCatcher<Result, Void>() {
	    private boolean setterPerformed = false;

	    @Override
	    protected Result tryToDoInBackground() {
		if (!b || isSelected()) {
		    return null;
		}

		// additional creation ov old value. maybe should be removed
		final Object oldValue = (entity instanceof BufferedPropertyWrapper) ? entity.get(propertyName) : null;

		entity.set(propertyName, choice);
		setterPerformed = true;

		// if (!boundedMetaProperty().isValid()) {
		// return null;
		// }

		// additional firing. maybe should be removed
		if (entity instanceof BufferedPropertyWrapper && ((BufferedPropertyWrapper) entity).getSubjectBean().getChangeSupport() != null) {
		    ((BufferedPropertyWrapper) entity).getSubjectBean().getChangeSupport().firePropertyChange(propertyName, oldValue, choice, CheckingStrategy.CHECK_NOTHING, false);
		}
		return null;
	    }

	    @Override
	    protected void tryToDone() {
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

    // Safety Check ***********************************************************

    /**
     * Throws an UnsupportedOperationException if the group is not {@code null}. You need not and must not use a ButtonGroup with a set of RadioButtonAdapters. RadioButtonAdapters
     * form a group by sharing the same subject ValueModel.
     *
     * @param group
     *            the <code>ButtonGroup</code> that will be rejected
     *
     * @throws UnsupportedOperationException
     *             if the group is not {@code null}.
     */
    @Override
    public void setGroup(final ButtonGroup group) {
	if (group != null) {
	    throw new UnsupportedOperationException("You need not and must not use a ButtonGroup " + "with a set of RadioButtonAdapters. These form "
		    + "a group by sharing the same subject ValueModel.");
	}
    }

    /**
     * Updates this adapter's selected state to reflect whether the subject holds the selected value or not. Does not modify the subject value.
     */
    private void updateSelectedState() {
	final boolean subjectHoldsChoiceValue = choice.equals(entity.get(propertyName));
	super.setSelected(subjectHoldsChoiceValue);
    }

    /**
     * Updates this adapter's selected state to reflect whether the subject holds the selected value or not. Does not modify the subject value.
     */
    private void updateSelectedStateBy(final Object newValue) {
	final boolean newValueHoldsChoiceValue = choice.equals(newValue);
	super.setSelected(newValueHoldsChoiceValue);
    }

    // Event Handling *********************************************************

    /**
     * Handles changes in the subject's value.
     */
    private final class SubjectValueChangeHandler implements Binder.SubjectValueChangeHandler {

	/**
	 * The subject value has changed. Updates this adapter's selected state to reflect whether the subject holds the choice value or not.
	 *
	 * @param evt
	 *            the property change event fired by the subject
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
	    if (boundedMetaProperty() == null || boundedMetaProperty().isValid()) {
		updateSelectedState();
	    } else {
		if (isOnKeyTyped()) {
		    // in this case selected state need to be updated by incorrect attempt value
		    updateSelectedStateBy(boundedMetaProperty().getLastInvalidValue());
		} else {
		    // in this case we should update state not by "actual value" and not by "last incorrect attempt value"
		    // but by "the new attempted value"
		    updateSelectedStateBy(evt.getNewValue());
		}
	    }
	    updateToolTip();
	}

    }

    /**
     * updates the editable state of the component based on the Editable state of the bound Property
     */
    public void updateEditable() {
	if (boundedMetaProperty() != null) {
	    SwingUtilities.invokeLater(new Runnable() {
		@Override
		public void run() {
		    boundedValidationLayer.getView().setEnabled(boundedMetaProperty().isEditable());
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

    @Override
    public void updateByActualOrLastIncorrectValue() {
	if (boundedMetaProperty() == null || boundedMetaProperty().isValid()) {
	    updateSelectedState();
	} else {
	    final boolean subjectIncorrectValueHoldsChoiceValue = choice.equals(boundedMetaProperty().getLastInvalidValue());
	    super.setSelected(subjectIncorrectValueHoldsChoiceValue);
	}
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

    @Override
    public String getPropertyName() {
	return propertyName;
    }
}
