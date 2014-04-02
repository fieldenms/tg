package ua.com.fielden.platform.swing.components.textfield;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * <code>UpperCaseTextField</code> is a JTextField descendant that converts its content to uppercase.
 * 
 * @author TG Team
 * 
 */
public class UpperCaseTextField extends AbstractTextField {
    private static final long serialVersionUID = -1957909441638026543L;

    public UpperCaseTextField(final Options... options) {
        super(options);
    }

    public UpperCaseTextField(final String text, final Options... options) {
        super(text, options);
    }

    @Override
    protected Document createDefaultModel() {
        return new UpperCaseDocument();
    }

    /**
     * Document which converts inserted string to uppercase.
     * 
     * @author TG Team
     * 
     */
    private static class UpperCaseDocument extends PlainDocument {
        private static final long serialVersionUID = 0;

        @Override
        public void insertString(final int offs, final String str, final AttributeSet a) throws BadLocationException {
            if (str == null) {
                return;
            }
            super.insertString(offs, str.toUpperCase(), a);
        }
    }
}