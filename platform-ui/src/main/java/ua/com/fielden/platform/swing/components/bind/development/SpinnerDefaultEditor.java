package ua.com.fielden.platform.swing.components.bind.development;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory.SpecialFormattedField;
import ua.com.fielden.platform.utils.Pair;

/**
 * This DefaultEditor modified to use special Bind API formatted field and to control property change event's firing.
 *
 * @author Jhou
 *
 */
public class SpinnerDefaultEditor extends JSpinner.DefaultEditor {
    private static final long serialVersionUID = -3691001676531098943L;

    private static final Action DISABLED_ACTION = new DisabledAction();
    private final Class<?> propertyType;
    private final Logger logger = Logger.getLogger(this.getClass());
    private final JFormattedTextField formattedTextField;

    public SpinnerDefaultEditor(final JSpinner spinner, final boolean decimal, final Class<?> propertyType) {
	super(spinner);

	this.propertyType = propertyType;

	dismiss(spinner);
	remove(getTextField());

	final Pair<NumberFormat, NumberFormat> formats = ComponentFactory.createNumberFormats(decimal);
	final JFormattedTextField ftf = new SpecialFormattedField(ComponentFactory.createNumberFormatterFactory(formats.getKey(), formats.getValue()));
	this.formattedTextField = ftf;
	ftf.setName("Spinner.formattedTextField");
	ftf.setValue(spinner.getValue());
	ftf.addPropertyChangeListener(this);
	ftf.setEditable(true);
	ftf.setInheritsPopupMenu(true);

	final String toolTipText = spinner.getToolTipText();
	if (toolTipText != null) {
	    ftf.setToolTipText(toolTipText);
	}

	add(ftf);

	setLayout(this);
	spinner.addChangeListener(this);

	// We want the spinner's increment/decrement actions to be
	// active vs those of the JFormattedTextField. As such we
	// put disabled actions in the JFormattedTextField's actionmap.
	// A binding to a disabled action is treated as a nonexistant
	// binding.
	final ActionMap ftfMap = ftf.getActionMap();

	if (ftfMap != null) {
	    ftfMap.put("increment", DISABLED_ACTION);
	    ftfMap.put("decrement", DISABLED_ACTION);
	}
    }

    public void addAgain(final JSpinner spinner) {
	spinner.addChangeListener(this);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent e) {
	//	    super.propertyChange(e);
	final JSpinner spinner = getSpinner();
	if (spinner == null) {
	    // Indicates we aren't installed anywhere.
	    return;
	}

	final Object source = e.getSource();
	final String name = e.getPropertyName();
	if ((source instanceof JFormattedTextField) && "value".equals(name)) {
	    //            final Object lastValue = spinner.getValue();

	    // Try to set the new value
	    try {
		spinner.setValue(SpinnerConnector.convertNumberValueObtainedFromFormattedField(getTextField().getValue(), propertyType));
	    } catch (final IllegalArgumentException iae) {
		logger.debug("VALUE [" + getTextField().getValue() + "] OF TYPE [" + ((getTextField().getValue() == null) ? null : getTextField().getValue().getClass())
			+ "] COULD NOT BE PROPAGATED TO SPINNER FROM ITS TEXT FIELD");
		//                // SpinnerModel didn't like new value, reset
		//                try {
		//                    ((JFormattedTextField)source).setValue(lastValue);
		//                } catch (final IllegalArgumentException iae2) {
		//                    // Still bogus, nothing else we can do, the
		//                    // SpinnerModel and JFormattedTextField are now out
		//                    // of sync.
		//                }
	    }
	}

    }

    /**
     * An Action implementation that is always disabled.
     */
    private static class DisabledAction implements Action {
	public Object getValue(final String key) {
	    return null;
	}

	public void putValue(final String key, final Object value) {
	}

	public void setEnabled(final boolean b) {
	}

	public boolean isEnabled() {
	    return false;
	}

	public void addPropertyChangeListener(final PropertyChangeListener l) {
	}

	public void removePropertyChangeListener(final PropertyChangeListener l) {
	}

	public void actionPerformed(final ActionEvent ae) {
	}
    }

    @Override
    public boolean requestFocusInWindow() {
        return this.formattedTextField.requestFocusInWindow();
    }
}
