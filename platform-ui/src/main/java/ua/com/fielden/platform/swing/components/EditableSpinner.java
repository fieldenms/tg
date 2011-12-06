package ua.com.fielden.platform.swing.components;

import java.awt.Component;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeListener;

/**
 * A spinner UI control with property <code>editable</code>. This property ensures that while control is enabled its content cannot be changed.
 * By default JSpinner can only be disabled, but not made not editable.
 *
 * @author TG Team
 *
 */
public class EditableSpinner extends JSpinner {

    private boolean editable = true;
    private final Number actualValue;

    public EditableSpinner(final SpinnerModel model, final Number actualValue) {
	setModel(new WrapperModel(model));
	this.actualValue = actualValue;
    }

    /** Overridden to provide correct mouse listener assignment not only to inner text editor, but also to arrow buttons etc. */
    @Override
    public synchronized void addMouseListener(final MouseListener listener) {
	// get the actual text field and assign listener:
	if (getEditor() instanceof DefaultEditor) {
	    ((DefaultEditor) getEditor()).getTextField().addMouseListener(listener);
	}
	// get actual arrow buttons and assign listener:
	for (final Component c : getComponents()) {
	    c.addMouseListener(listener);
	}
    }

    public void resetSpinnerModelValue(){
	getModel().setValue(actualValue);
    }

    @Override
    public void updateUI() {
	super.updateUI();
	for (final Component c : getComponents()) {
	    if (c instanceof AbstractButton) {
		c.setEnabled(editable);
	    }
	}
    }

    public boolean isEditable() {
	return editable;
    }

    public void setEditable(final boolean editable) {
	this.editable = editable;
	updateUI();
    }

    /**
     *  A wrapper model used by this editable spinner, which redirects the calls to the provided model, but overrides the behaviour of the setValue method by taking into account the editable property of the spinner.
     */
    private class WrapperModel implements SpinnerModel {
	private SpinnerModel innerModel;

	public WrapperModel(final SpinnerModel baseModel) {
	    innerModel = baseModel;
	}

	@Override
	public Object getValue() {
	    return innerModel.getValue();
	}

	@Override
	public void setValue(final Object value) {
	    if (editable) {
		innerModel.setValue(value);
	    }

	}

	@Override
	public Object getNextValue() {
	    return innerModel.getNextValue();
	}

	@Override
	public Object getPreviousValue() {
	    return innerModel.getPreviousValue();
	}

	@Override
	public void addChangeListener(final ChangeListener l) {
	    innerModel.addChangeListener(l);
	}

	@Override
	public void removeChangeListener(final ChangeListener l) {
	    innerModel.removeChangeListener(l);
	}
    }

}
