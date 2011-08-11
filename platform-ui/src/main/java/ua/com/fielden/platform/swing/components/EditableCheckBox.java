package ua.com.fielden.platform.swing.components;

import javax.swing.JCheckBox;

import ua.com.fielden.platform.swing.components.bind.ToggleButtonAdapter;

/**
 * A checkbox UI control with property <code>editable</code>. This property ensures that while control is enabled its content cannot be changed.
 * By default JCheckBox can only be disabled, but not made not editable.
 * <p>
 * The implementation relies on the fact that checkbox's model is based on {@link ToggleButtonAdapter}.
 *
 * @author TG Team
 *
 */
public class EditableCheckBox extends JCheckBox {

    private boolean editable = true;

    public EditableCheckBox(final String text) {
	super(text);
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(final boolean editable) {
        this.editable = editable;
        if (getModel() instanceof ToggleButtonAdapter) {
            ((ToggleButtonAdapter) getModel()).setEditable(editable);
        }
    }
}
