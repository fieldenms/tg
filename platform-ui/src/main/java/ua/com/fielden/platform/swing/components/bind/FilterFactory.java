package ua.com.fielden.platform.swing.components.bind;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

/**
 * Factory to create TextComponents filters, like Integer, Numeric, Date etc.
 * 
 * @author jhou
 * 
 */
public class FilterFactory {

    protected FilterFactory() {
    }

    public static final AbstractDocumentFilter createIntegerDocumentFilter() {
	return new IntegerDocumentFilter();
    }

    public static final AbstractDocumentFilter createNumericDocumentFilter() {
	return new NumericDocumentFilter();
    }

    public static final AbstractDocumentFilter createStringDocumentFilter() {
	return new StringDocumentFilter();
    }

    public static final AbstractDocumentFilter createStringWithEnterDocumentFilter() {
	return new StringWithEnterDocumentFilter();
    }

    protected static abstract class AbstractDocumentFilter extends DocumentFilter implements Serializable {
	private static final long serialVersionUID = -729812571298150237L;

	@Override
	public void insertString(final DocumentFilter.FilterBypass fb, final int offset, final String text, final AttributeSet attr) throws BadLocationException {
	    if (text == null) {
		return;
	    } else {
		replace(fb, offset, 0, text, attr);
	    }
	}

	@Override
	public void replace(final DocumentFilter.FilterBypass fb, final int offset, final int length, final String text, final AttributeSet attrs) throws BadLocationException {
	    final Document doc = fb.getDocument();
	    final int currentLength = doc.getLength();
	    final String currentContent = doc.getText(0, currentLength);
	    final String before = currentContent.substring(0, offset);
	    final String after = currentContent.substring(length + offset, currentLength);
	    final String newValue = before + (text == null ? "" : text) + after;
	    if (allowInput(newValue)) {
		fb.replace(offset, length, text, attrs);
	    }
	}

	@Override
	public void remove(final DocumentFilter.FilterBypass fb, final int offset, final int length) throws BadLocationException {
	    replace(fb, offset, length, "", null);
	}

	public abstract boolean allowInput(String value);
    }

    /**
     * This is a document filter, which allows only integers (digits) to be typed.
     * 
     * @author Jhou
     * 
     */
    private static class IntegerDocumentFilter extends AbstractDocumentFilter {
	private static final long serialVersionUID = 6684954556153897458L;

	@Override
	public boolean allowInput(final String value) {
	    if ("".equals(value)) {
		return true;
	    }
	    final Pattern p = Pattern.compile("-?\\d*");
	    final Matcher m = p.matcher(value);
	    return m.matches();
	}
    }

    /**
     * This is a document filter, which allows only numeric characters (digits and a dot) to be typed.
     * 
     * @author 01es
     * 
     */
    private static class NumericDocumentFilter extends AbstractDocumentFilter {
	private static final long serialVersionUID = 6684954556153897458L;

	@Override
	public boolean allowInput(final String value) {
	    if ("".equals(value)) {
		return true;
	    }
	    final Pattern p = Pattern.compile("-?\\d*\\.?\\d*");
	    final Matcher m = p.matcher(value);
	    return m.matches();
	}
    }

    /**
     * This is a document filter, which allows any string without "\n\r" to be typed.
     * 
     * @author Jhou
     * 
     */
    private static class StringDocumentFilter extends AbstractDocumentFilter {
	private static final long serialVersionUID = 6684954556153897458L;

	@Override
	public boolean allowInput(final String value) {
	    if ("".equals(value)) {
		return true;
	    }
	    final Pattern p = Pattern.compile("[^\n\r]*");
	    final Matcher m = p.matcher(value);
	    return m.matches();
	}
    }

    /**
     * This is a document filter, which allows any string to be typed.
     * 
     * @author Jhou
     * 
     */
    private static class StringWithEnterDocumentFilter extends AbstractDocumentFilter {
	private static final long serialVersionUID = 6684954556153897458L;

	@Override
	public boolean allowInput(final String value) {
	    if ("".equals(value)) {
		return true;
	    }
	    final Pattern p = Pattern.compile(".*", Pattern.DOTALL);
	    final Matcher m = p.matcher(value);
	    return m.matches();
	}
    }

}
