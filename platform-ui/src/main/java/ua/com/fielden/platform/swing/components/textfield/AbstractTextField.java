package ua.com.fielden.platform.swing.components.textfield;

import javax.swing.JTextField;
import javax.swing.text.Document;

/**
 * <code>AbstractTextField</code> is a JTextField descendant with custom document model.
 * 
 * @author TG Team
 * 
 */
public abstract class AbstractTextField extends JTextField {
    private static final long serialVersionUID = 0;

    protected AbstractTextField(final Options... options) {
        this(null, options);
    }

    protected AbstractTextField(final String text, final Options... options) {
        for (final Options option : options) {
            option.set(this);
        }
        if (text != null) {
            setText(text);
        }
    }

    @Override
    protected abstract Document createDefaultModel();

    @Override
    public void setText(final String t) {
        final String currValue = getText();
        super.setText(t);
        firePropertyChange("text", currValue, getText());
    }
}