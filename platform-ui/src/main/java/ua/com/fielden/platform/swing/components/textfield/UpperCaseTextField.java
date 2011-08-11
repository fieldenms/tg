package ua.com.fielden.platform.swing.components.textfield;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * <code>UpperCaseTextField</code> is a JTextField descendant that converts its content to uppercase.
 * 
 * @author 01es
 * 
 */
public class UpperCaseTextField extends JTextField {
    private static final long serialVersionUID = 0;

    public UpperCaseTextField(final String text) {
	super(text);
    }

    public UpperCaseTextField(final Options... options) {
	for (final Options option : options) {
	    option.set(this);
	}
    }

    public UpperCaseTextField(final String text, final Options... options) {
	this(options);
	setText(text);
    }

    protected Document createDefaultModel() {
	return new UpperCaseDocument();
    }

    @Override
    public void setText(final String t) {
	final String currValue = getText();
	super.setText(t);
	firePropertyChange("text", currValue, getText());
    }

    /**
     * 
     * 
     * @author 01es
     * 
     */
    private static class UpperCaseDocument extends PlainDocument {
	private static final long serialVersionUID = 0;

	public void insertString(final int offs, final String str, final AttributeSet a) throws BadLocationException {
	    if (str == null) {
		return;
	    }
	    super.insertString(offs, str.toUpperCase(), a);
	}
    }
}