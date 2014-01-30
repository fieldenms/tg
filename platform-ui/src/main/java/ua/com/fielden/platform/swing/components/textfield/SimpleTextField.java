package ua.com.fielden.platform.swing.components.textfield;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * <code>SimpleTextField</code> is a {@link AbstractTextField} descendant that does nothing to its text.
 *
 * @author TG Team
 *
 */
public class SimpleTextField extends AbstractTextField {
    private static final long serialVersionUID = -1957909441638026543L;

    public SimpleTextField(final Options... options) {
	super(options);
    }

    public SimpleTextField(final String text, final Options... options) {
	super(text, options);
    }

    @Override
    protected Document createDefaultModel() {
	return new SimpleDocument();
    }

    /**
     * Document which does nothing to inserted string.
     *
     * @author TG Team
     *
     */
    private static class SimpleDocument extends PlainDocument {
	private static final long serialVersionUID = 0;

	@Override
	public void insertString(final int offs, final String str, final AttributeSet a) throws BadLocationException {
	    if (str == null) {
		return;
	    }
	    super.insertString(offs, str, a);
	}
    }
}